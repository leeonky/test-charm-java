package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.node.DALNode;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.interpreter.InterpreterException;
import com.github.leeonky.util.Sneaky;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import org.apache.tika.Tika;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.lang.Long.parseLong;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class Inspector {
    private static Inspector inspector = null;
    private static Mode mode = null;
    private final Javalin javalin;
    private final CountDownLatch serverReadyLatch = new CountDownLatch(1);
    private final Set<DAL> instances = new LinkedHashSet<>();
    //   TODO refactor
    private final Map<String, WsContext> clientConnections = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientMonitors = new ConcurrentHashMap<>();
    private final Map<String, DALInstance> dalInstances = new ConcurrentHashMap<>();
    private static Supplier<Object> defaultInput = () -> null;

    public Inspector() {
        DALInstance defaultInstance = new DALInstance(() -> defaultInput.get(), DAL.create("Try It!", InspectorExtension.class), "");
        defaultInstance.running = false;
        dalInstances.put("Try It!", defaultInstance);
        javalin = Javalin.create(config -> config.addStaticFiles("/public", Location.CLASSPATH))
                .events(event -> event.serverStarted(serverReadyLatch::countDown));
        requireNonNull(javalin.jettyServer()).setServerPort(getServerPort());
        javalin.get("/", ctx -> ctx.redirect("/index.html"));
        javalin.post("/api/execute", ctx -> ctx.html(execute(ctx.queryParam("name"), ctx.body())));
        javalin.post("/api/exchange", ctx -> exchange(ctx.queryParam("session"), ctx.body()));
        javalin.post("/api/pass", ctx -> pass(ctx.queryParam("name")));
        javalin.post("/api/release", ctx -> release(ctx.queryParam("name")));
        javalin.post("/api/release-all", ctx -> releaseAll());
        javalin.get("/api/request", ctx -> ctx.html(request(ctx.queryParam("name"))));
        javalin.get("/attachments", ctx -> responseAttachment(ctx.queryParam("name"), Integer.parseInt(ctx.queryParam("index")), ctx));
        javalin.ws("/ws/exchange", ws -> {
            ws.onConnect(ctx -> {
                clientConnections.put(ctx.getSessionId(), ctx);
                sendInstances(ctx);
            });
            ws.onClose(ctx -> clientConnections.remove(ctx.getSessionId()));
        });
        javalin.start();
    }

    private void responseAttachment(String name, int index, Context ctx) {
        DALInstance dalInstance = dalInstances.get(name);
        if (dalInstance != null) {
            Watch watch = dalInstance.watches.get(index);
            if (watch instanceof DALInstance.BinaryWatch) {

                DALInstance.BinaryWatch binaryWatch = (DALInstance.BinaryWatch) watch;
                String contentType = Sneaky.get(() -> new Tika().detect(binaryWatch.binary()));
                ctx.contentType(contentType == null ? "application/octet-stream" : contentType);
                ctx.result(binaryWatch.binary());
            }
        }
    }

    public static void watch(DAL dal, String property, Data value) {
        if (inspector != null)
            inspector.watchInner(dal, property, value);
    }

    private void watchInner(DAL dal, String property, Data value) {
        if (inspector.calledFromInspector())
            dalInstances.get(dal.getName()).watch(property, value);
    }

    private void pass(String name) {
        if (!name.equals("Try It!")) {
            DALInstance remove = dalInstances.remove(name);
            if (remove != null)
                remove.pass();
        }
    }

    private void waitForReady() {
        Sneaky.run(serverReadyLatch::await);
    }

    private static int getServerPort() {

        return getFirstPresent(() -> ofNullable(System.getenv("DAL_INSPECTOR_PORT")),
                () -> ofNullable(System.getProperty("dal.inspector.port")))
                .map(Integer::parseInt)
                .orElse(10082);
    }

    public static void ready() {
        inspector.waitForReady();
    }

    private void releaseAll() {
        for (String instanceName : new ArrayList<>(dalInstances.keySet()))
            release(instanceName);
    }

    private void release(String name) {
        if (!name.equals("Try It!")) {
            DALInstance remove = dalInstances.remove(name);
            if (remove != null)
                remove.release();
        }
    }

    public static void setDefaultMode(Mode mode) {
        Inspector.mode = mode;
    }

    private void exchange(String session, String body) {
        if (clientConnections.containsKey(session)) {
            clientMonitors.put(session, Arrays.stream(body.trim().split("\\n")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet()));

            for (DALInstance dalInstance : dalInstances.values()) {
                if (dalInstance.running)
                    clientConnections.get(session).send(ObjectWriter.serialize(new HashMap<String, String>() {{
                        put("request", dalInstance.dal.getName());
                    }}));
            }
        }
    }

    public static class DALInstance {
        private final Supplier<Object> input;
        private boolean running = true;
        private boolean pass = false;
        private final DAL dal;
        private final String code;
        private final List<Watch> watches = new ArrayList<>();

        public DALInstance(Supplier<Object> input, DAL dal, String code) {
            this.input = input;
            this.dal = dal;
            this.code = code;
        }

        public DALInstance(Data<?> inputData, DAL dal, String code) {
            input = inputData::value;
            this.dal = dal;
            this.code = code;
        }

        public String execute(String code) {
            watches.clear();
            Map<String, Object> response = new HashMap<>();
            DALRuntimeContext runtimeContext = dal.getRuntimeContextBuilder().build(input::get, null);
            try {
                response.put("root", runtimeContext.getThis().dump());
                DALNode dalNode = dal.compileSingle(code, runtimeContext);
                response.put("inspect", dalNode.inspect());
                response.put("result", dalNode.evaluateData(runtimeContext).dump());
            } catch (InterpreterException e) {
                response.put("error", e.show(code) + "\n\n" + e.getMessage());
            }
            response.put("watches", watches.stream().map(Watch::collect).collect(Collectors.toList()));
            return ObjectWriter.serialize(response);
        }

        public boolean hold() {
            System.err.println("Waiting for DAL inspector release...");
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

                System.err.println("\tDAl inspector running at:");

                while (interfaces.hasMoreElements()) {
                    Enumeration<InetAddress> inetAddresses = interfaces.nextElement().getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress address = inetAddresses.nextElement();
                        System.err.printf("\t\thttp://%s:%d%n", address.getHostAddress(), getServerPort());
                    }
                }
            } catch (Exception ignore) {
            }
            //        TODO use sempahore to wait for the result
            Instant now = Instant.now();
            while (running && stillWaiting(now))
                Sneaky.run(() -> Thread.sleep(20));
//            TODO use logger
            System.err.println("DAL inspector released with pass: " + pass);
            return pass;
        }

        public void release() {
            running = false;
        }

        public void pass() {
            pass = true;
            release();
        }

        private byte[] getBytes(Data<?> data) {
            return getFirstPresent(
                    () -> data.cast(byte[].class),
                    () -> data.cast(InputStream.class).map(stream -> Sneaky.get(() -> {
                        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                            int size;
                            byte[] data1 = new byte[1024];
                            while ((size = stream.read(data1, 0, data1.length)) != -1)
                                buffer.write(data1, 0, size);
                            return buffer.toByteArray();
                        }
                    })),
                    () -> data.cast(Byte[].class).map(bytes -> {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        for (Byte b : bytes)
                            stream.write(b);
                        return stream.toByteArray();
                    })
            ).orElse(null);
        }

        public void watch(String property, Data value) {
            byte[] bytes = getBytes(value);
            if (bytes != null) {
                watches.add(new BinaryWatch(property, bytes));
            } else
                watches.add(new DefaultWatch(property, value));
        }


        public class DefaultWatch implements Watch {
            private final String property;
            private final String value;

            public DefaultWatch(String property, Data value) {
                this.property = property;
                this.value = value.dump();
            }

            @Override
            public Map<String, Object> collect() {
                return new HashMap<String, Object>() {{
                    put("property", property);
                    put("type", "DEFAULT");
                    put("value", value);
                }};
            }
        }

        private class BinaryWatch implements Watch {
            private final String property;
            private final int index;
            private final byte[] binary;

            public BinaryWatch(String property, byte[] value) {
                this.property = property;
                index = watches.size();
                binary = new byte[value.length];
                System.arraycopy(value, 0, binary, 0, value.length);
            }

            public byte[] binary() {
                return binary;
            }

            @Override
            public Map<String, Object> collect() {
                return new HashMap<String, Object>() {{
                    put("property", property);
                    put("type", "BINARY");
                    put("url", "/attachments?name=" + dal.getName() + "&index=" + index + "&tm=" + Instant.now().getEpochSecond());
                }};
            }
        }
    }

    public boolean inspectInner(DAL dal, Data input, String code) {
        if (calledFromInspector())
            return false;
//        lock inspect by name
//        check mode
        if (currentMode() == Mode.FORCED) {
            DALInstance dalInstance = new DALInstance(input, dal, code);
            dalInstances.put(dal.getName(), dalInstance);

//            List<WsContext> monitored = clientMonitors.entrySet().stream().filter(e -> e.getValue().contains(dal.getName()))
//                    .map(o -> clientConnections.get(o.getKey()))
//                    .collect(Collectors.toList());
//            TODO check monitor flag
            for (WsContext wsContext : clientConnections.values()) {
                wsContext.send(ObjectWriter.serialize(new HashMap<String, String>() {{
                    put("request", dal.getName());
                }}));
            }

            return dalInstance.hold();

        } else {
//        TODO refactor
            List<WsContext> monitored = clientMonitors.entrySet().stream().filter(e -> e.getValue().contains(dal.getName()))
                    .map(o -> clientConnections.get(o.getKey()))
                    .collect(Collectors.toList());
            if (!monitored.isEmpty()) {
                DALInstance dalInstance = new DALInstance(input, dal, code);
                dalInstances.put(dal.getName(), dalInstance);
                for (WsContext wsContext : monitored) {
                    wsContext.send(ObjectWriter.serialize(new HashMap<String, String>() {{
                        put("request", dal.getName());
                    }}));
                }
                return dalInstance.hold();
            }
            return false;
        }
    }

    private static boolean stillWaiting(Instant now) {
        String waitingTime = System.getenv("DAL_INSPECTOR_WAITING_TIME");
        return (waitingTime == null ? 3600 * 1000 * 24 : parseLong(waitingTime))
                > Duration.between(now, Instant.now()).toMillis();
    }

    public static boolean inspect(DAL dal, Data input, String code) {
        if (currentMode() != Mode.DISABLED)
            return inspector.inspectInner(dal, input, code);
        return false;
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

    private boolean calledFromInspector() {
        for (StackTraceElement stack : Thread.currentThread().getStackTrace())
            if (DALInstance.class.getName().equals(stack.getClassName()))
                return true;
        return false;
    }

    public static void main(String[] args) {
        launch();
    }

    interface Watch {
        Map<String, Object> collect();
    }
}