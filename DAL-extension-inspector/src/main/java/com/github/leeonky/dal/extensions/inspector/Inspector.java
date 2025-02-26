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

import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class Inspector {
    private static Inspector inspector = null;
    private static Mode mode = null;
    private final Javalin javalin;
    private final CountDownLatch serverReadyLatch;
    private final Set<DAL> instances = new LinkedHashSet<>();
    //   TODO refactor
    private final Map<String, WsContext> clientConnections = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientMonitors = new ConcurrentHashMap<>();
    private final Map<String, DalInstance> dalInstances = new ConcurrentHashMap<>();
    private static Supplier<Object> defaultInput = () -> null;

    public Inspector() {
        dalInstances.put("Try It!", new DalInstance(() -> defaultInput.get(), DAL.create(InspectorExtension.class), ""));

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
                pugConfiguration.renderTemplate(pugConfiguration.getTemplate("public" + filePath), model), ".pug", ".PNG", ".Png");

        serverReadyLatch = new CountDownLatch(1);
        javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH))
                .events(event -> event.serverStarted(serverReadyLatch::countDown));
        requireNonNull(javalin.jettyServer()).setServerPort(getServerPort());
        javalin.get("/", ctx -> ctx.render("/index.pug", Collections.emptyMap()));
        javalin.post("/api/execute", ctx -> ctx.html(execute(ctx.queryParam("name"), ctx.body())));
        javalin.post("/api/exchange", ctx -> exchange(ctx.queryParam("session"), ctx.body()));
        javalin.post("/api/release", ctx -> release(ctx.queryParam("name")));
        javalin.post("/api/release-all", ctx -> releaseAll());
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

    private static int getServerPort() {

        return getFirstPresent(() -> ofNullable(System.getenv("DAL_INSPECTOR_PORT")),
                () -> ofNullable(System.getProperty("dal.inspector.port")))
                .map(Integer::parseInt)
                .orElse(10082);
    }

    private void releaseAll() {
        for (String instanceName : new ArrayList<>(dalInstances.keySet()))
            release(instanceName);
    }

    private void release(String name) {
        DalInstance remove = dalInstances.remove(name);
        if (remove != null)
            remove.release();
    }

    public static void setDefaultMode(Mode mode) {
        Inspector.mode = mode;
    }

    private void exchange(String session, String body) {
        if (clientConnections.containsKey(session)) {
            clientMonitors.put(session, Arrays.stream(body.trim().split("\\n")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet()));
        }
    }

    public static class DalInstance {
        private final Supplier<Object> input;
        private boolean running = true;
        private final DAL dal;
        private final String code;

        public DalInstance(Supplier<Object> input, DAL dal, String code) {
            this.input = input;
            this.dal = dal;
            this.code = code;
        }

        public String execute(String code) {
            Map<String, String> response = new HashMap<>();
            Object inputObject = input.get();
            RuntimeContextBuilder.DALRuntimeContext runtimeContext = dal.getRuntimeContextBuilder().build(inputObject);
            try {
                response.put("root", runtimeContext.wrap(inputObject).dumpAll());
                response.put("inspect", dal.compileSingle(code, runtimeContext).inspect());
                response.put("result", runtimeContext.wrap(dal.evaluate(inputObject, code)).dumpAll());
            } catch (InterpreterException e) {
                response.put("error", e.show(code) + "\n\n" + e.getMessage());
            }
            return ObjectWriter.serialize(response);
        }

        public void hold() {
//            TODO use logger
            System.err.println("Waiting for inspector response");
            //        TODO use sempahore to wait for the result
            while (running)
                Suppressor.run(() -> Thread.sleep(20));
//            TODO use logger
            System.err.println("inspector released");
        }

        public void release() {
            running = false;
        }
    }

    public void inspectInner(DAL dal, Object input, String code) {
        if (isRecursive())
            return;
//        lock inspect by name
//        check mode
        if (currentMode() == Mode.FORCED) {
            DalInstance dalInstance = new DalInstance(() -> input, dal, code);
            dalInstances.put(dal.getName(), dalInstance);

            List<WsContext> monitored = clientMonitors.entrySet().stream().filter(e -> e.getValue().contains(dal.getName()))
                    .map(o -> clientConnections.get(o.getKey()))
                    .collect(Collectors.toList());
            for (WsContext wsContext : monitored) {
                wsContext.send(ObjectWriter.serialize(new HashMap<String, String>() {{
                    put("request", dal.getName());
                }}));
            }

            dalInstance.hold();

        } else {
//        TODO refactor
            List<WsContext> monitored = clientMonitors.entrySet().stream().filter(e -> e.getValue().contains(dal.getName()))
                    .map(o -> clientConnections.get(o.getKey()))
                    .collect(Collectors.toList());
            if (!monitored.isEmpty()) {
                DalInstance dalInstance = new DalInstance(() -> input, dal, code);
                dalInstances.put(dal.getName(), dalInstance);
                for (WsContext wsContext : monitored) {
                    wsContext.send(ObjectWriter.serialize(new HashMap<String, String>() {{
                        put("request", dal.getName());
                    }}));
                }

                dalInstance.hold();
            }
        }
    }

    public static void inspect(DAL dal, Object input, String code) {
        if (currentMode() != Mode.DISABLED)
            inspector.inspectInner(dal, input, code);
    }

    private String request(String name) {
//       TODO reject other request
        return dalInstances.get(name).code;
    }

    private String execute(String name, String code) {
        return dalInstances.get(name).execute(code);
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
        ctx.send(ObjectWriter.serialize(new HashMap<String, Object>() {{
            put("instances", instances.stream().map(DAL::getName).collect(Collectors.toSet()));
            put("session", ctx.getSessionId());
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

    public static Mode currentMode() {
        return getFirstPresent(() -> ofNullable(mode),
                () -> ofNullable(System.getenv("DAL_INSPECTOR_MODE")).map(Mode::valueOf),
                () -> ofNullable(System.getProperty("dal.inspector.mode")).map(Mode::valueOf))
                .orElse(Mode.DISABLED);
    }

    public enum Mode {
        DISABLED, FORCED, AUTO
    }

    private boolean isRecursive() {
        for (StackTraceElement stack : Thread.currentThread().getStackTrace())
            if (DalInstance.class.getName().equals(stack.getClassName()))
                return true;
        return false;
    }

    public static void main(String[] args) {
        launch();
    }
}