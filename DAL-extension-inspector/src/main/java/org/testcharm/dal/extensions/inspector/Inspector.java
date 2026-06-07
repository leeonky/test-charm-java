package org.testcharm.dal.extensions.inspector;

import org.testcharm.dal.DAL;
import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.interpreter.InterpreterException;
import org.testcharm.util.Sneaky;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Optional.ofNullable;
import static org.testcharm.util.Sneaky.sneakyGet;
import static org.testcharm.util.function.Extension.getFirstPresent;

public class Inspector {
    private static final String WS_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static Inspector inspector = null;
    private static Mode mode = null;

    private final CountDownLatch serverReadyLatch = new CountDownLatch(1);
    private final Set<DAL> instances = new LinkedHashSet<>();
    private final Map<String, ClientConnection> clientConnections = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> clientMonitors = new ConcurrentHashMap<>();
    private final Map<String, DALInstance> dalInstances = new ConcurrentHashMap<>();
    private final HttpWsServer server;
    private static Supplier<Object> defaultInput = () -> null;

    public Inspector() {
        DALInstance defaultInstance = new DALInstance(() -> defaultInput.get(), DAL.create("Try It!", InspectorExtension.class), "");
        defaultInstance.running = false;
        dalInstances.put("Try It!", defaultInstance);

        server = new HttpWsServer(getServerPort(), "/ws/exchange", this::upgradeWebSocket);
        registerRoutes(server);
        server.start();
        serverReadyLatch.countDown();
    }

    private void registerRoutes(HttpWsServer httpWsServer) {
        httpWsServer.requestHandler("GET", "/", request -> HttpResponse.redirect("/index.html"));
        httpWsServer.requestHandler("POST", "/api/execute", request -> {
            String name = request.query("name");
            return HttpResponse.xml(execute(name, request.bodyAsString()));
        });
        httpWsServer.requestHandler("POST", "/api/exchange", request -> {
            exchange(request.query("session"), request.bodyAsString());
            return HttpResponse.ok();
        });
        httpWsServer.requestHandler("POST", "/api/pass", request -> {
            pass(request.query("name"));
            return HttpResponse.ok();
        });
        httpWsServer.requestHandler("POST", "/api/release", request -> {
            release(request.query("name"));
            return HttpResponse.ok();
        });
        httpWsServer.requestHandler("POST", "/api/release-all", request -> {
            releaseAll();
            return HttpResponse.ok();
        });
        httpWsServer.requestHandler("GET", "/api/request",
                request -> HttpResponse.text(this.request(request.query("name"))));
        httpWsServer.requestHandler("GET", "/attachments", request -> {
            Attachment attachment = responseAttachment(request.query("name"), parseInt(request.query("index")));
            return HttpResponse.binary(attachment.body, attachment.contentType);
        });
        httpWsServer.defaultGetHandler(request -> staticResource(request.path()));
    }

    private Attachment responseAttachment(String name, int index) {
        DALInstance dalInstance = Objects.requireNonNull(dalInstances.get(name));
        DALInstance.BinaryWatch binaryWatch = (DALInstance.BinaryWatch) dalInstance.watches.get(index);
        byte[] bytes = binaryWatch.binary();
        String contentType = Sneaky.get(() -> URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes)));
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return new Attachment(bytes, contentType);
    }

    public static void watch(DAL dal, String property, Data value) {
        inspector.watchInner(dal, property, value);
    }

    private void watchInner(DAL dal, String property, Data value) {
        dalInstances.get(dal.getName()).watch(property, value);
    }

    private void pass(String name) {
        dalInstances.remove(name).pass();
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
        if (!"Try It!".equals(name)) {
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
                if (dalInstance.running) {
                    sendSafe(clientConnections.get(session), ObjectWriter.serialize(new HashMap<String, String>() {{
                        put("request", dalInstance.dal.getName());
                    }}));
                }
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
        private final Object constants;

        public DALInstance(Supplier<Object> input, DAL dal, String code) {
            this.input = input;
            this.dal = dal;
            this.code = code;
            constants = null;
        }

        public DALInstance(Data<?> inputData, DAL dal, String code, Object constants) {
            input = inputData::value;
            this.dal = dal;
            this.code = code;
            this.constants = constants;
        }

        public String execute(String code) {
            watches.clear();
            Map<String, Object> response = new HashMap<>();
            DALRuntimeContext runtimeContext = dal.getRuntimeContextBuilder().build(input::get, null, constants);
            try {
                response.put("root", runtimeContext.getThis().dump());
                DALNode dalNode = dal.compileSingle(code, runtimeContext);
                response.put("inspect", dalNode.inspect());
                response.put("constants", constants == null ? "" : runtimeContext.constants().dump());
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
            Instant now = Instant.now();
            while (running && stillWaiting(now))
                Sneaky.run(() -> Thread.sleep(20));
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
                    () -> data.cast(InputStream.class).map(sneakyGet(stream -> {
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
            property = uniqName(property);
            byte[] bytes = getBytes(value);
            if (bytes != null) {
                watches.add(new BinaryWatch(property, bytes));
            } else
                watches.add(new DefaultWatch(property, value));
        }

        private String uniqName(String property) {
            String newProperty = property;
            for (int i = 1; containName(newProperty); i++)
                newProperty = String.format("%s (%d)", property, i);
            return newProperty;
        }

        private boolean containName(String name) {
            return watches.stream().anyMatch(p -> p.property().equals(name));
        }

        public class DefaultWatch implements Watch {
            private final String property;
            private final String value;

            public DefaultWatch(String property, Data value) {
                this.property = property;
                this.value = value.dump();
            }

            @Override
            public String property() {
                return property;
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
            public String property() {
                return property;
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

    public boolean inspectInner(DAL dal, Data input, String code, Object constants) {
        if (calledFromInspector())
            return false;
        if (currentMode() == Mode.FORCED) {
            DALInstance dalInstance = new DALInstance(input, dal, code, constants);
            dalInstances.put(dal.getName(), dalInstance);

            for (ClientConnection client : clientConnections.values()) {
                sendSafe(client, ObjectWriter.serialize(new HashMap<String, String>() {{
                    put("request", dal.getName());
                }}));
            }

            return dalInstance.hold();

        } else {
            List<ClientConnection> monitored = clientMonitors.entrySet().stream().filter(e -> e.getValue().contains(dal.getName()))
                    .map(o -> clientConnections.get(o.getKey()))
                    .filter(c -> c != null && c.isOpen())
                    .collect(Collectors.toList());
            if (!monitored.isEmpty()) {
                DALInstance dalInstance = new DALInstance(input, dal, code, constants);
                dalInstances.put(dal.getName(), dalInstance);
                for (ClientConnection client : monitored) {
                    sendSafe(client, ObjectWriter.serialize(new HashMap<String, String>() {{
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

    public static boolean inspect(DAL dal, Data input, String code, Object constants) {
        if (currentMode() != Mode.DISABLED)
            return inspector.inspectInner(dal, input, code, constants);
        return false;
    }

    private String request(String name) {
        DALInstance instance = dalInstances.get(name);
        return instance == null ? "" : instance.code;
    }

    private String execute(String name, String code) {
        DALInstance instance = dalInstances.get(name);
        return instance == null ? "" : instance.execute(code);
    }

    public static void register(DAL dal) {
        inspector.addInstance(dal);
    }

    private void addInstance(DAL dal) {
        instances.add(dal);
        for (ClientConnection client : clientConnections.values()) {
            sendInstances(client);
        }
    }

    private void sendInstances(ClientConnection client) {
        sendSafe(client, ObjectWriter.serialize(new HashMap<String, Object>() {{
            put("instances", instances.stream().map(DAL::getName).collect(Collectors.toSet()));
            put("session", client.sessionId());
        }}));
    }

    private void stop() {
        server.stop();
        for (ClientConnection client : new ArrayList<>(clientConnections.values())) {
            client.close();
        }
        clientConnections.clear();
        clientMonitors.clear();
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

        String property();
    }

    private static class Attachment {
        private final byte[] body;
        private final String contentType;

        private Attachment(byte[] body, String contentType) {
            this.body = body;
            this.contentType = contentType;
        }
    }

    private void sendSafe(ClientConnection client, String message) {
        if (client == null) {
            return;
        }
        try {
            client.sendText(message);
        } catch (IOException e) {
            clientConnections.remove(client.sessionId());
            clientMonitors.remove(client.sessionId());
            client.close();
        }
    }

    private HttpResponse staticResource(String path) {
        String resourcePath = "/public" + ("/".equals(path) ? "/index.html" : path);
        InputStream resource = Inspector.class.getResourceAsStream(resourcePath);
        if (resource == null) {
            return HttpResponse.notFound();
        }
        byte[] body = Sneaky.get(() -> readFully(resource));
        return HttpResponse.binary(body, contentTypeFor(resourcePath));
    }

    private String contentTypeFor(String resourcePath) {
        if (resourcePath.endsWith(".html")) {
            return "text/html; charset=utf-8";
        }
        if (resourcePath.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (resourcePath.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (resourcePath.endsWith(".svg")) {
            return "image/svg+xml";
        }
        return "application/octet-stream";
    }

    private void upgradeWebSocket(Socket socket, HttpRequest request) throws Exception {
        String websocketKey = request.header("sec-websocket-key");
        if (websocketKey == null) {
            socket.close();
            return;
        }

        String accept = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1")
                .digest((websocketKey + WS_GUID).getBytes(StandardCharsets.ISO_8859_1)));

        OutputStream output = socket.getOutputStream();
        String response = "HTTP/1.1 101 Switching Protocols\r\n"
                + "Upgrade: websocket\r\n"
                + "Connection: Upgrade\r\n"
                + "Sec-WebSocket-Accept: " + accept + "\r\n\r\n";
        output.write(response.getBytes(StandardCharsets.ISO_8859_1));
        output.flush();

        String sessionId = UUID.randomUUID().toString();
        ClientConnection client = new ClientConnection(sessionId, socket);
        clientConnections.put(sessionId, client);
        sendInstances(client);

        try {
            client.readLoop();
        } finally {
            clientConnections.remove(sessionId);
            clientMonitors.remove(sessionId);
            client.close();
        }
    }

    private byte[] readFully(InputStream in) throws IOException {
        try (InputStream input = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }
}
