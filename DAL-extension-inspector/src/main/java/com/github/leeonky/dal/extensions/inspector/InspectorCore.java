package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.Suppressor;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.ClasspathTemplateLoader;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.websocket.WsContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.util.Optional.ofNullable;

public class InspectorCore {
    private static Mode mode;

    public static void setMode(Mode mode) {
        InspectorCore.mode = mode;
    }

    private boolean running = true;
    private DAL dal;
    private Object input;
    private String code;

    private Javalin javalin;
    private CountDownLatch serverReadyLatch;
    private final Map<String, WsContext> clientConnections = new ConcurrentHashMap<>();
    private final ApiProvider apiProvider = new ApiProvider();

    public InspectorCore() {
        JadeConfiguration jadeConfiguration = new JadeConfiguration();
        jadeConfiguration.setCaching(false);
        jadeConfiguration.setTemplateLoader(new ClasspathTemplateLoader());
        JavalinRenderer.register((filePath, model, context) ->
                jadeConfiguration.renderTemplate(jadeConfiguration.getTemplate("public" + filePath), model), ".pug", ".PNG", ".Png");

        serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH))
                .events(event -> event.serverStarted(serverReadyLatch::countDown));
        Objects.requireNonNull(javalin.jettyServer()).setServerPort(10081);
        javalin.get("/", ctx -> ctx.render("/index.pug", Collections.emptyMap()));
        javalin.get("/api/sync", ctx -> ctx.status(200).html(apiProvider.sync()));
        javalin.post("/api/resume", ctx -> apiProvider.resume());
        javalin.post("/api/execute", ctx -> ctx.status(200).html(apiProvider.execute(ctx.body())));
        javalin.ws("/ws/ping", ws -> {
            ws.onConnect(ctx -> {
                clientConnections.put(ctx.getSessionId(), ctx);
                if (running)
                    for (WsContext wsContext : clientConnections.values()) {
                        wsContext.send("start");
                    }
            });
            ws.onClose(ctx -> clientConnections.remove(ctx.getSessionId()));
        });
        javalin.start();
        Suppressor.run(serverReadyLatch::await);
    }

    public void exit() {
        javalin.close();
    }

    public void inspectViaMode(DAL dal, Object input, String code) {
        if (currentMode() == Mode.DAL_INSPECTOR_ASSERT_FORCED)
            inspect(dal, input, code);
    }

    public void inspect(DAL dal, Object input, String code) {
        if (!isRecursive()) {
            this.running = true;
            this.dal = dal;
            this.input = input;
            this.code = code;

            for (WsContext wsContext : clientConnections.values()) {
                wsContext.send("start");
            }

            while (running)
                Suppressor.run(() -> Thread.sleep(100));
        }
    }

    public Mode currentMode() {
        return getFirstPresent(() -> ofNullable(mode),
                () -> ofNullable(System.getenv("DAL_INSPECTOR_ASSERT_MODE")).map(Mode::valueOf),
                () -> ofNullable(System.getProperty("dal.inspector.assert-mode")).map(Mode::valueOf))
                .orElse(Mode.DAL_INSPECTOR_ASSERT_DISABLED);
    }

    private boolean isRecursive() {
        for (StackTraceElement stack : Thread.currentThread().getStackTrace())
            if (ApiProvider.class.getName().equals(stack.getClassName()))
                return true;
        return false;
    }

    public enum Mode {
        DAL_INSPECTOR_ASSERT_DISABLED, DAL_INSPECTOR_ASSERT_FORCED
    }

    public class ApiProvider {
        public String sync() {
            Map<String, Object> response = new HashMap<>();
            response.put("mode", currentMode().name());
//            response.put("instances", Inspector.getInstances().stream().map(DAL::getName).collect(Collectors.toSet()));
            response.put("code", code);
            if (dal != null)
                response.put("current", dal.getName());
            return ObjectWriter.serialize(response);
        }

        public String execute(String code) {
            Map<String, String> response = new HashMap<>();
            RuntimeContextBuilder.DALRuntimeContext runtimeContext = dal.getRuntimeContextBuilder().build(input);
            try {
                response.put("root", runtimeContext.wrap(input).dumpAll());
                response.put("inspect", dal.compileSingle(code, runtimeContext).inspect());
                response.put("result", runtimeContext.wrap(dal.evaluate(input, code)).dumpAll());
            } catch (InterpreterException e) {
                response.put("error", e.show(code) + "\n\n" + e.getMessage());
            }
            return ObjectWriter.serialize(response);
        }

        public void resume() {
            running = false;
        }
    }

}
