package org.testcharm.cucumber.swarm.master;

import com.sun.net.httpserver.HttpServer;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.RestfulContext;
import org.testcharm.util.Sneaky;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class RestfulServer {
    HttpServer httpServer;
    private Logger log = LoggerFactory.getLogger(RestfulServer.class);

    public RestfulServer(int port) {
        httpServer = Sneaky.get(() -> HttpServer.create(new InetSocketAddress(port), 0));
    }

    public void start() {
        log.info(() -> String.format("Starting restful server on %s...",
                httpServer.getAddress().getAddress().getHostAddress() + ":" + httpServer.getAddress().getPort()));
        httpServer.start();
        Sneaky.run(this::testServer);
        log.info(() -> "Restful server started");
    }

    private void testServer() throws InterruptedException {
        Duration timeout = Duration.ofSeconds(5);
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", httpServer.getAddress().getPort()), 200);
                return;
            } catch (IOException e) {
                Thread.sleep(50);
            }
        }
        throw new IllegalStateException("Restful server did not become ready within " + timeout);
    }

    public void requestHandler(String method, String path, Consumer<RestfulContext> handler) {
        httpServer.createContext(path, exchange -> {
            RestfulContext restfulContext = new RestfulContext(exchange);
            if (!restfulContext.exchange.getRequestMethod().equals(method))
                restfulContext.error(405);
            else {
                try {
                    handler.accept(restfulContext);
                } catch (ServerCloseException e) {
                    restfulContext.error(409);
                } catch (NoSuchElementException e) {
                    restfulContext.error(404);
                } catch (Exception e) {
                    restfulContext.error(500, e.toString());
                }
            }
            restfulContext.exchange.close();
        });
    }

    public void shutdown() {
        log.info(() -> "Shutting down restful server...");
        httpServer.stop(0);
        log.info(() -> "Restful server shut down");
    }
}