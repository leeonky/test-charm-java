package org.testcharm.cucumber.swarm.master;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcharm.cucumber.restful.RestfulStep;
import org.testcharm.cucumber.swarm.RestfulContext;

import java.util.NoSuchElementException;

class RestfulServerTest {
    RestfulServer server = new RestfulServer(9000);
    RestfulStep restfulStep = new RestfulStep();

    @BeforeEach
    void start_server() {
        server.start();
        restfulStep.setBaseUrl("http://localhost:9000");
    }

    @AfterEach
    void shutdown_server() {
        server.shutdown();
    }

    @SneakyThrows
    @Test
    void unmatched_method_should_return_405() {
        server.requestHandler("GET", "/test", RestfulContext::responseOk);

        restfulStep.postInJson("/test", "{}");

        restfulStep.responseShouldBe("code= 405");
    }

    @Test
    @SneakyThrows
    void no_such_element_should_return_404() {
        server.requestHandler("GET", "/test", ctx -> {
            throw new NoSuchElementException();
        });

        restfulStep.get("/test");

        restfulStep.responseShouldBe("code= 404");
    }

    @Test
    @SneakyThrows
    void unexpected_exception_should_return_500() {
        server.requestHandler("GET", "/test", ctx -> {
            throw new RuntimeException("Unexpected error");
        });

        restfulStep.get("/test");

        restfulStep.responseShouldBe("code= 500");
    }
}