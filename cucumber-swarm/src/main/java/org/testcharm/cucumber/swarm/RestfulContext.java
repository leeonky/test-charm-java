package org.testcharm.cucumber.swarm;

import com.sun.net.httpserver.HttpExchange;
import org.testcharm.util.Sneaky;

public class RestfulContext {
    public HttpExchange exchange;

    public RestfulContext(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public void responseOk(String content) {
        responseOk(content.getBytes());
    }

    public void responseOk(byte[] bytes) {
        Sneaky.run(() -> {
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        });
    }

    public void error(int code) {
        Sneaky.run(() -> exchange.sendResponseHeaders(code, 0));
    }

    public void error(int code, String message) {
        Sneaky.run(() -> {
            byte[] bytes = message.getBytes();
            exchange.sendResponseHeaders(code, bytes.length);
            exchange.getResponseBody().write(bytes);
        });
    }

    public String header(String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }
}
