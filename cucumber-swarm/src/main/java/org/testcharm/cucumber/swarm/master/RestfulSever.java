package org.testcharm.cucumber.swarm.master;

import com.sun.net.httpserver.HttpServer;
import org.testcharm.cucumber.swarm.RestfulContext;
import org.testcharm.util.Sneaky;

import java.net.InetSocketAddress;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

public class RestfulSever {
    HttpServer httpServer;

    public RestfulSever(int port) {
        httpServer = Sneaky.get(() -> HttpServer.create(new InetSocketAddress(port), 0));
    }

    public void start() {
        httpServer.start();
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
                }
            }
            restfulContext.exchange.close();
        });
    }

    public void exit() {
        httpServer.stop(0);
    }
}