package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.Suppressor;
import de.neuland.pug4j.PugConfiguration;
import de.neuland.pug4j.template.TemplateLoader;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.websocket.WsContext;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

public class Inspector {
    private static Inspector inspector = null;
    private final Javalin javalin;
    private final CountDownLatch serverReadyLatch;
    private final Set<DAL> instances = new LinkedHashSet<>();
    private final Map<String, WsContext> clientConnections = new ConcurrentHashMap<>();
    private final DAL defaultDal = DAL.create(InspectorExtension.class);
    private static Supplier<Object> defaultInput = () -> null;

    public Inspector() {
        PugConfiguration pugConfiguration = new PugConfiguration();
        pugConfiguration.setTemplateLoader(new TemplateLoader() {
            @Override
            public long getLastModified(String name) throws IOException {
                return -1;
            }

            @Override
            public Reader getReader(String name) throws IOException {
                return new InputStreamReader(requireNonNull(currentThread().getContextClassLoader().getResourceAsStream(name)),
                        StandardCharsets.UTF_8);
            }

            @Override
            public String getExtension() {
                return "pug";
            }

            @Override
            public String getBase() {
                return "";
            }
        });

        JavalinRenderer.register((filePath, model, context) ->
                pugConfiguration.renderTemplate(pugConfiguration.getTemplate("public/" + filePath), model), ".pug", ".PNG", ".Png");

        serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH))
                .events(event -> event.serverStarted(serverReadyLatch::countDown));
        requireNonNull(javalin.jettyServer()).setServerPort(10081);
        javalin.get("/", ctx -> ctx.render("/index.pug", Collections.emptyMap()));
//        TODO execute by name
        javalin.post("/api/execute", ctx -> ctx.html(execute(ctx.queryParam("name"), ctx.body())));
        javalin.get("/api/request", ctx -> ctx.html(request(ctx.queryParam("name"))));
        javalin.ws("/ws/exchange", ws -> {
            ws.onConnect(ctx -> {
                clientConnections.put(ctx.getSessionId(), ctx);
                sendInstances(ctx);
            });
            ws.onClose(ctx -> clientConnections.remove(ctx.getSessionId()));
        });
        javalin.start();
        Suppressor.run(serverReadyLatch::await);
    }

    public static class DalInstance {
        private final Object input;
        private final boolean running = true;
        private final DAL dal;
        private final String code;

        public DalInstance(Object input, DAL dal, String code) {
            this.input = input;
            this.dal = dal;
            this.code = code;
        }
    }

    private final Map<String, DalInstance> dalInstances = new ConcurrentHashMap<>();

    public void inspectInner(DAL dal, Object input, String code) {
//        TODO  stack over flow
//        lock inspect by name
//        check mode
        DalInstance dalInstance = new DalInstance(input, dal, code);
        dalInstances.put(dal.getName(), dalInstance);
        for (WsContext wsContext : clientConnections.values()) {
            wsContext.send(ObjectWriter.serialize(new HashMap<String, String>() {{
                put("request", dal.getName());
            }}));
        }

//        TODO use sempahore to wait for the result
        while (dalInstance.running)
            Suppressor.run(() -> Thread.sleep(20));
    }

    public static void inspect(DAL dal, Object input, String code) {
        inspector.inspectInner(dal, input, code);
    }

    private String request(String name) {
        return dalInstances.get(name).code;
    }

    private String execute(String name, String code) {
        if (name.equals("Try It!")) {
            Map<String, String> response = new HashMap<>();
            Object inputObject = Inspector.defaultInput.get();
            RuntimeContextBuilder.DALRuntimeContext runtimeContext = defaultDal.getRuntimeContextBuilder().build(inputObject);
            try {
                response.put("root", runtimeContext.wrap(inputObject).dumpAll());
                response.put("inspect", defaultDal.compileSingle(code, runtimeContext).inspect());
                response.put("result", runtimeContext.wrap(defaultDal.evaluate(inputObject, code)).dumpAll());
            } catch (InterpreterException e) {
                response.put("error", e.show(code) + "\n\n" + e.getMessage());
            }
            return ObjectWriter.serialize(response);
        } else {
            DalInstance dalInstance = dalInstances.get(name);
            Map<String, String> response = new HashMap<>();
            Object inputObject = dalInstance.input;
            RuntimeContextBuilder.DALRuntimeContext runtimeContext = dalInstance.dal.getRuntimeContextBuilder().build(inputObject);
            try {
                response.put("root", runtimeContext.wrap(inputObject).dumpAll());
                response.put("inspect", dalInstance.dal.compileSingle(code, runtimeContext).inspect());
                response.put("result", runtimeContext.wrap(dalInstance.dal.evaluate(inputObject, code)).dumpAll());
            } catch (InterpreterException e) {
                response.put("error", e.show(code) + "\n\n" + e.getMessage());
            }
            return ObjectWriter.serialize(response);
        }
    }

    public static void register(DAL dal) {
        inspector.addInstance(dal);
    }

    private void addInstance(DAL dal) {
        instances.add(dal);
        for (WsContext ctx : clientConnections.values()) {
            sendInstances(ctx);
        }
    }

    private void sendInstances(WsContext ctx) {
        ctx.send(ObjectWriter.serialize(new HashMap<String, Iterable<String>>() {{
            put("instances", instances.stream().map(DAL::getName).collect(Collectors.toSet()));
        }}));
    }

    private void stop() {
        javalin.close();
    }

    public static void launch() {
        if (inspector == null) {
            inspector = new Inspector();
        }
    }

    public static void shutdown() {
        if (inspector != null) {
            inspector.stop();
            inspector = null;
        }
    }

    public static void setDefaultInput(Supplier<Object> supplier) {
        defaultInput = supplier;
    }

    public static void main(String[] args) {
        launch();
    }
}