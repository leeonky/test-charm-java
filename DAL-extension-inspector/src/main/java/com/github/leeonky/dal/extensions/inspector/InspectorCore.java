package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.node.DALNode;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.util.Suppressor;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.ClasspathTemplateLoader;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.websocket.WsContext;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static com.github.leeonky.dal.Accessors.get;
import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

public class InspectorCore {

    private static Mode mode;

    public static void setMode(Mode mode) {
        InspectorCore.mode = mode;
    }

    private boolean running = true;
    private DAL dal = DAL.create(InspectorExtension.class);
    private Object input;
    private String code;

    private Javalin javalin;
    private CountDownLatch serverReadyLatch;
    private final Map<String, WsContext> clientConnections = new ConcurrentHashMap<>();

    private ApiProvider apiProvider = new ApiProvider();

    public InspectorCore() {
        JadeConfiguration jadeConfiguration = new JadeConfiguration();
        jadeConfiguration.setCaching(false);
        jadeConfiguration.setTemplateLoader(new ClasspathTemplateLoader());
        JavalinRenderer.register((filePath, model, context) ->
                jadeConfiguration.renderTemplate(jadeConfiguration.getTemplate("public" + filePath), model), ".pug", ".PNG", ".Png");

        start();
    }

    public void start() {
        serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH))
                .events(event -> event.serverStarted(serverReadyLatch::countDown));
        Objects.requireNonNull(javalin.jettyServer()).setServerPort(10081);
        javalin.get("/", ctx -> ctx.render("/index.pug", Collections.emptyMap()));
        javalin.get("/api/fetch-code", ctx -> ctx.status(200).html(apiProvider.fetchCode()));
        javalin.post("/api/execute", ctx -> ctx.status(200).html(apiProvider.execute(ctx.body())));
        javalin.ws("/ws/ping", ws -> {
            ws.onConnect(ctx -> clientConnections.put(ctx.getSessionId(), ctx));
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
            this.dal = dal;
            this.input = input;
            this.code = code;
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
        public String fetchCode() {
            return code;
        }

        public String execute(String code) {
            try {
                RuntimeContextBuilder.DALRuntimeContext runtimeContext = dal.getRuntimeContextBuilder().build(input);
                DALNode node = dal.compileSingle(code, runtimeContext);
                if (node.isVerification()) {
                    expect(input).use(dal).should(code);
                } else {
                    Object result = get(code).by(dal).from(input);
                    return runtimeContext.wrap(result).dumpAll();
                }
                return "";
            } catch (Throwable e) {
                return format("%s:%s", e.getClass().getName(), e.getMessage());
            }
        }
    }
}
