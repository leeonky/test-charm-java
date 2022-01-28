package com.github.leeonky.cucumber;

import com.github.dreamhead.moco.RequestHit;
import com.github.dreamhead.moco.Runner;
import com.github.leeonky.cucumber.restful.CustomPicoFactory;
import com.github.leeonky.cucumber.restful.RestfulStep;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

import static com.github.dreamhead.moco.Moco.*;
import static com.github.dreamhead.moco.MocoRequestHit.once;
import static com.github.dreamhead.moco.MocoRequestHit.requestHit;
import static com.github.dreamhead.moco.Runner.runner;
import static org.assertj.core.api.Assertions.assertThat;

public class Steps {
    private final RestfulStep restfulStep;

    private Runner runner = null;
    private RequestHit requestHit;
    private String requestedBaseUrl;

    public Steps(RestfulStep restfulStep) {
        this.restfulStep = restfulStep;
    }

    @Given("base url {string}")
    public void base_url(String baseUrl) {
        CustomPicoFactory.lookupAction = s -> startMoco(s, baseUrl);
        restfulStep.setBaseUrl(baseUrl);
    }

    @SneakyThrows
    private void startMoco(@NotNull String s, String baseUrl) {
        assertThat(new URL(baseUrl).getHost()).isEqualTo(s);
        requestedBaseUrl = baseUrl;
        (runner = runner(httpServer(80, requestHit = requestHit()))).start();
    }

    @Then("{string} got a GET request on {string}")
    public void got_a_get_request_on(String url, String path) {
        requestHit.verify(and(by(uri(path)), by(method("GET"))), once());
        assertThat(url).isEqualTo(requestedBaseUrl);
    }

    @After
    void stopMoco() {
        if (runner != null)
            runner.stop();
    }
}
