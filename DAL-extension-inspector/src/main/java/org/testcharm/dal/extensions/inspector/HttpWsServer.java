package org.testcharm.dal.extensions.inspector;

import org.testcharm.util.Sneaky;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

class HttpWsServer {
    void defaultGetHandler(Function<HttpRequest, HttpResponse> handler) {
        defaultHandler(request -> "GET".equals(request.method())
                ? handler.apply(request)
                : HttpResponse.notFound());
    }

    @FunctionalInterface
    interface WebSocketUpgradeHandler {
        void upgrade(Socket socket, HttpRequest request) throws Exception;
    }

    private final int port;
    private final String webSocketPath;
    private final WebSocketUpgradeHandler webSocketUpgradeHandler;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final List<Route> routes = new ArrayList<>();
    private volatile boolean running;
    private volatile Function<HttpRequest, HttpResponse> defaultHandler = request -> HttpResponse.notFound();
    private ServerSocket serverSocket;

    HttpWsServer(int port, String webSocketPath, WebSocketUpgradeHandler webSocketUpgradeHandler) {
        this.port = port;
        this.webSocketPath = webSocketPath;
        this.webSocketUpgradeHandler = webSocketUpgradeHandler;
    }

    void requestHandler(String method, String path, Function<HttpRequest, HttpResponse> handler) {
        routes.add(new Route(method, path, handler));
    }

    void defaultHandler(Function<HttpRequest, HttpResponse> handler) {
        defaultHandler = handler;
    }

    void start() {
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

    void stop() {
        running = false;
        if (serverSocket != null) {
            Sneaky.run(serverSocket::close);
        }
        executor.shutdownNow();
    }

    private void handle(Socket socket) {
        try {
            HttpRequest request = HttpRequest.read(socket.getInputStream());
            if (request == null) {
                socket.close();
                return;
            }

            if (request.isWebSocketUpgrade() && webSocketPath.equals(request.path())) {
                webSocketUpgradeHandler.upgrade(socket, request);
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
        for (Route route : routes) {
            if (route.matches(request)) {
                return route.handle(request);
            }
        }
        return defaultHandler.apply(request);
    }

    private static class Route {
        private final String method;
        private final String path;
        private final Function<HttpRequest, HttpResponse> handler;

        private Route(String method, String path, Function<HttpRequest, HttpResponse> handler) {
            this.method = method;
            this.path = path;
            this.handler = handler;
        }

        private boolean matches(HttpRequest request) {
            return method.equals(request.method()) && path.equals(request.path());
        }

        private HttpResponse handle(HttpRequest request) {
            return handler.apply(request);
        }
    }
}
