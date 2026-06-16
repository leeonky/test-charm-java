package org.testcharm.cucumber.swarm.master;

import com.sun.net.httpserver.HttpServer;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.testcharm.cucumber.swarm.RestfulContext;
import org.testcharm.util.Sneaky;

import java.net.InetSocketAddress;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static java.time.Duration.ofSeconds;

public class RestfulServer {
    private static final Logger log = LoggerFactory.getLogger(RestfulServer.class);
    private final HttpServer httpServer;

    public RestfulServer(int port) {
        httpServer = Sneaky.get(() -> HttpServer.create(new InetSocketAddress(port), 0));
    }

    public void start() {
        log.info(() -> String.format("Starting restful server on %s...",
                httpServer.getAddress().getAddress().getHostAddress() + ":" + httpServer.getAddress().getPort()));
        httpServer.start();
        new SocketProbe("Restful server", new InetSocketAddress("localhost", httpServer.getAddress().getPort()), ofSeconds(5)).testConnection();
        log.info(() -> "Restful server started");
    }

    public void requestHandler(String method, String path, Consumer<RestfulContext> handler) {
        httpServer.createContext(path, exchange -> {
            RestfulContext restfulContext = new RestfulContext(exchange);
            if (!restfulContext.exchange.getRequestMethod().equals(method))
                restfulContext.error(405);
            else {
                try {
                    handler.accept(restfulContext);
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