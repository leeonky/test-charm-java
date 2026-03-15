package org.testcharm.dal.extensions.inspector;

import org.testcharm.dal.DAL;
import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.interpreter.InterpreterException;
import org.testcharm.util.Sneaky;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static java.util.Optional.ofNullable;
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

        server = new HttpWsServer(getServerPort());
        server.start();
        serverReadyLatch.countDown();
    }

    private Attachment responseAttachment(String name, int index) {
        DALInstance dalInstance = dalInstances.get(name);
        if (dalInstance == null || index < 0 || index >= dalInstance.watches.size()) {
            return null;
        }

        Watch watch = dalInstance.watches.get(index);
        if (!(watch instanceof DALInstance.BinaryWatch)) {
            return null;
        }

        DALInstance.BinaryWatch binaryWatch = (DALInstance.BinaryWatch) watch;
        byte[] bytes = binaryWatch.binary();
        String contentType = Sneaky.get(() -> URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes)));
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return new Attachment(bytes, contentType);
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
        if (!"Try It!".equals(name)) {
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
                    .filter(c -> c != null && c.open)
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
            put("session", client.sessionId);
        }}));
    }

    private void stop() {
        server.stop();
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
            clientConnections.remove(client.sessionId);
            clientMonitors.remove(client.sessionId);
            client.close();
        }
    }

    private class HttpWsServer {
        private final int port;
        private final ExecutorService executor = Executors.newCachedThreadPool();
        private volatile boolean running;
        private ServerSocket serverSocket;

        private HttpWsServer(int port) {
            this.port = port;
        }

        private void start() {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to start inspector server on port " + port, e);
            }
            executor.execute(() -> {
                while (running) {
                    try {
                        Socket socket = serverSocket.accept();
                        socket.setTcpNoDelay(true);
                        executor.execute(() -> handle(socket));
                    } catch (IOException e) {
                        if (running) {
                            throw new IllegalStateException("Inspector server accept failed", e);
                        }
                    }
                }
            });
        }

        private void stop() {
            running = false;
            if (serverSocket != null) {
                Sneaky.run(serverSocket::close);
            }
            for (ClientConnection client : new ArrayList<>(clientConnections.values())) {
                client.close();
            }
            clientConnections.clear();
            clientMonitors.clear();
            executor.shutdownNow();
        }

        private void handle(Socket socket) {
            try {
                HttpRequest request = HttpRequest.read(socket.getInputStream());
                if (request == null) {
                    socket.close();
                    return;
                }

                if (request.isWebSocketUpgrade() && "/ws/exchange".equals(request.path)) {
                    upgradeWebSocket(socket, request);
                    return;
                }

                HttpResponse response = route(request);
                response.write(socket.getOutputStream());
                socket.close();
            } catch (Exception ignore) {
                Sneaky.run(socket::close);
            }
        }

        private HttpResponse route(HttpRequest request) {
            if ("GET".equals(request.method) && "/".equals(request.path)) {
                return HttpResponse.redirect("/index.html");
            }
            if ("POST".equals(request.method) && "/api/execute".equals(request.path)) {
                String name = request.query("name");
                return HttpResponse.xml(execute(name, request.bodyAsString()));
            }
            if ("POST".equals(request.method) && "/api/exchange".equals(request.path)) {
                exchange(request.query("session"), request.bodyAsString());
                return HttpResponse.ok();
            }
            if ("POST".equals(request.method) && "/api/pass".equals(request.path)) {
                pass(request.query("name"));
                return HttpResponse.ok();
            }
            if ("POST".equals(request.method) && "/api/release".equals(request.path)) {
                release(request.query("name"));
                return HttpResponse.ok();
            }
            if ("POST".equals(request.method) && "/api/release-all".equals(request.path)) {
                releaseAll();
                return HttpResponse.ok();
            }
            if ("GET".equals(request.method) && "/api/request".equals(request.path)) {
                return HttpResponse.text(request(request.query("name")));
            }
            if ("GET".equals(request.method) && "/attachments".equals(request.path)) {
                int index;
                try {
                    index = Integer.parseInt(request.query("index"));
                } catch (Exception e) {
                    return HttpResponse.notFound();
                }
                Attachment attachment = responseAttachment(request.query("name"), index);
                if (attachment == null) {
                    return HttpResponse.notFound();
                }
                return HttpResponse.binary(attachment.body, attachment.contentType);
            }

            if ("GET".equals(request.method)) {
                return staticResource(request.path);
            }
            return HttpResponse.notFound();
        }

        private HttpResponse staticResource(String path) {
            String normalized = "/".equals(path) ? "/index.html" : path;
            if (normalized.contains("..")) {
                return HttpResponse.notFound();
            }
            String resourcePath = "/public" + normalized;
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

    private static class ClientConnection {
        private final String sessionId;
        private final Socket socket;
        private final InputStream in;
        private final OutputStream out;
        private volatile boolean open = true;

        private ClientConnection(String sessionId, Socket socket) throws IOException {
            this.sessionId = sessionId;
            this.socket = socket;
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }

        private synchronized void sendText(String message) throws IOException {
            if (!open) {
                return;
            }
            byte[] payload = message.getBytes(StandardCharsets.UTF_8);
            writeFrame((byte) 0x1, payload);
        }

        private synchronized void sendPong(byte[] payload) throws IOException {
            if (!open) {
                return;
            }
            writeFrame((byte) 0xA, payload);
        }

        private synchronized void writeFrame(byte opcode, byte[] payload) throws IOException {
            int length = payload.length;
            out.write(0x80 | (opcode & 0x0F));
            if (length <= 125) {
                out.write(length);
            } else if (length <= 65535) {
                out.write(126);
                out.write((length >>> 8) & 0xFF);
                out.write(length & 0xFF);
            } else {
                out.write(127);
                for (int i = 7; i >= 0; i--) {
                    out.write((int) ((long) length >>> (8 * i)) & 0xFF);
                }
            }
            out.write(payload);
            out.flush();
        }

        private void readLoop() throws IOException {
            while (open) {
                int b0 = in.read();
                if (b0 == -1) {
                    return;
                }
                int b1 = in.read();
                if (b1 == -1) {
                    return;
                }

                int opcode = b0 & 0x0F;
                boolean masked = (b1 & 0x80) != 0;
                long payloadLength = b1 & 0x7F;

                if (payloadLength == 126) {
                    payloadLength = ((long) readByte() << 8) | readByte();
                } else if (payloadLength == 127) {
                    payloadLength = 0;
                    for (int i = 0; i < 8; i++) {
                        payloadLength = (payloadLength << 8) | readByte();
                    }
                }

                if (payloadLength > Integer.MAX_VALUE) {
                    throw new IOException("WebSocket payload too large");
                }

                byte[] maskKey = null;
                if (masked) {
                    maskKey = new byte[]{(byte) readByte(), (byte) readByte(), (byte) readByte(), (byte) readByte()};
                }

                byte[] payload = new byte[(int) payloadLength];
                readFully(payload);
                if (masked && maskKey != null) {
                    for (int i = 0; i < payload.length; i++) {
                        payload[i] = (byte) (payload[i] ^ maskKey[i % 4]);
                    }
                }

                if (opcode == 0x8) {
                    return;
                }
                if (opcode == 0x9) {
                    sendPong(payload);
                }
            }
        }

        private int readByte() throws IOException {
            int value = in.read();
            if (value == -1) {
                throw new EOFException();
            }
            return value & 0xFF;
        }

        private void readFully(byte[] bytes) throws IOException {
            int offset = 0;
            while (offset < bytes.length) {
                int read = in.read(bytes, offset, bytes.length - offset);
                if (read == -1) {
                    throw new EOFException();
                }
                offset += read;
            }
        }

        private void close() {
            open = false;
            Sneaky.run(socket::close);
        }
    }

    private static class HttpRequest {
        private final String method;
        private final String path;
        private final Map<String, String> headers;
        private final Map<String, String> queryParams;
        private final byte[] body;

        private HttpRequest(String method, String path, Map<String, String> headers, Map<String, String> queryParams, byte[] body) {
            this.method = method;
            this.path = path;
            this.headers = headers;
            this.queryParams = queryParams;
            this.body = body;
        }

        private String header(String name) {
            return headers.get(name.toLowerCase(Locale.ROOT));
        }

        private String query(String name) {
            return queryParams.getOrDefault(name, "");
        }

        private String bodyAsString() {
            return new String(body, StandardCharsets.UTF_8);
        }

        private boolean isWebSocketUpgrade() {
            String upgrade = header("upgrade");
            String connection = header("connection");
            return "websocket".equalsIgnoreCase(upgrade)
                    && connection != null
                    && connection.toLowerCase(Locale.ROOT).contains("upgrade");
        }

        private static HttpRequest read(InputStream in) throws IOException {
            byte[] headerBytes = readHeaders(in);
            if (headerBytes == null || headerBytes.length == 0) {
                return null;
            }
            String head = new String(headerBytes, StandardCharsets.ISO_8859_1);
            String[] lines = head.split("\\r\\n");
            if (lines.length == 0) {
                return null;
            }

            String[] requestLine = lines[0].split(" ");
            if (requestLine.length < 2) {
                return null;
            }
            String method = requestLine[0].trim();
            String target = requestLine[1].trim();

            Map<String, String> headers = new HashMap<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) {
                    continue;
                }
                int idx = line.indexOf(':');
                if (idx <= 0) {
                    continue;
                }
                headers.put(line.substring(0, idx).trim().toLowerCase(Locale.ROOT), line.substring(idx + 1).trim());
            }

            int contentLength = 0;
            if (headers.containsKey("content-length")) {
                try {
                    contentLength = Integer.parseInt(headers.get("content-length"));
                } catch (NumberFormatException ignore) {
                    contentLength = 0;
                }
            }
            byte[] body = new byte[Math.max(contentLength, 0)];
            if (contentLength > 0) {
                int offset = 0;
                while (offset < contentLength) {
                    int read = in.read(body, offset, contentLength - offset);
                    if (read == -1) {
                        throw new EOFException();
                    }
                    offset += read;
                }
            }

            int queryIndex = target.indexOf('?');
            String path = queryIndex < 0 ? target : target.substring(0, queryIndex);
            Map<String, String> query = queryIndex < 0 ? Collections.emptyMap() : parseQuery(target.substring(queryIndex + 1));

            return new HttpRequest(method, path, headers, query, body);
        }

        private static byte[] readHeaders(InputStream in) throws IOException {
            ByteArrayOutputStream headers = new ByteArrayOutputStream();
            int matched = 0;
            while (true) {
                int b = in.read();
                if (b == -1) {
                    return headers.size() == 0 ? null : headers.toByteArray();
                }
                headers.write(b);
                if ((matched == 0 && b == '\r')
                        || (matched == 1 && b == '\n')
                        || (matched == 2 && b == '\r')
                        || (matched == 3 && b == '\n')) {
                    matched++;
                    if (matched == 4) {
                        byte[] all = headers.toByteArray();
                        return Arrays.copyOf(all, all.length - 4);
                    }
                } else {
                    matched = b == '\r' ? 1 : 0;
                }

                if (headers.size() > 65536) {
                    throw new IOException("HTTP headers too large");
                }
            }
        }

        private static Map<String, String> parseQuery(String query) {
            Map<String, String> values = new HashMap<>();
            if (query == null || query.isEmpty()) {
                return values;
            }
            for (String item : query.split("&")) {
                int idx = item.indexOf('=');
                String key = idx < 0 ? item : item.substring(0, idx);
                String value = idx < 0 ? "" : item.substring(idx + 1);
                values.put(urlDecode(key), urlDecode(value));
            }
            return values;
        }

        private static String urlDecode(String value) {
            return Sneaky.get(() -> URLDecoder.decode(value, StandardCharsets.UTF_8.name()));
        }
    }

    private static class HttpResponse {
        private final int code;
        private final String status;
        private final byte[] body;
        private final Map<String, String> headers;

        private HttpResponse(int code, String status, byte[] body, Map<String, String> headers) {
            this.code = code;
            this.status = status;
            this.body = body;
            this.headers = headers;
        }

        private static HttpResponse ok() {
            return text("");
        }

        private static HttpResponse text(String value) {
            return withBody(200, "OK", value.getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");
        }

        private static HttpResponse xml(String value) {
            return withBody(200, "OK", value.getBytes(StandardCharsets.UTF_8), "application/xml; charset=utf-8");
        }

        private static HttpResponse binary(byte[] body, String contentType) {
            return withBody(200, "OK", body, contentType);
        }

        private static HttpResponse redirect(String location) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Location", location);
            headers.put("Content-Length", "0");
            return new HttpResponse(302, "Found", new byte[0], headers);
        }

        private static HttpResponse notFound() {
            return withBody(404, "Not Found", "Not Found".getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");
        }

        private static HttpResponse withBody(int code, String status, byte[] body, String contentType) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", contentType);
            headers.put("Content-Length", String.valueOf(body.length));
            return new HttpResponse(code, status, body, headers);
        }

        private void write(OutputStream out) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 ").append(code).append(' ').append(status).append("\r\n");
            sb.append("Connection: close\r\n");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }
            sb.append("\r\n");
            out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
            out.write(body);
            out.flush();
        }
    }
}
