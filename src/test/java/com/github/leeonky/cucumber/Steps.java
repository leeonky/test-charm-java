package com.github.leeonky.cucumber;

import com.github.leeonky.cucumber.restful.CustomPicoFactory;
import com.github.leeonky.cucumber.restful.RestfulStep;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.verify.VerificationTimes;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;

public class Steps {
    private final RestfulStep restfulStep;

    private String requestedBaseUrl;

    private ClientAndServer mockServer;

    public Steps(RestfulStep restfulStep) {
        this.restfulStep = restfulStep;
    }

    @Given("base url {string}")
    public void base_url(String baseUrl) {
        CustomPicoFactory.lookupAction = s -> startMockServer(s, baseUrl);
        restfulStep.setBaseUrl(baseUrl);
    }

    @SneakyThrows
    private void startMockServer(@NotNull String s, String baseUrl) {
        assertThat(new URL(baseUrl).getHost()).isEqualTo(s);
        requestedBaseUrl = baseUrl;
        mockServer = startClientAndServer(80);
    }

    @Then("{string} got a GET request on {string}")
    public void got_a_get_request_on(String url, String path) {
        mockServer.verify(request()
                        .withMethod("GET")
                        .withPath(path),
                VerificationTimes.once());
        assertThat(url).as(String.format("Expect %s to receive the request, but send to %s", url, requestedBaseUrl)).isEqualTo(requestedBaseUrl);
    }

    @After
    void stopMockServer() {
        if (mockServer != null)
            mockServer.stop();
    }
}
