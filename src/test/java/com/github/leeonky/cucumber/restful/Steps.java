package com.github.leeonky.cucumber.restful;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Format;
import org.mockserver.verify.VerificationTimes;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.github.leeonky.dal.extension.assertj.DALAssert.expect;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class Steps {
    private static final ClientAndServer mockServer = startClientAndServer(80);
    private final RestfulStep restfulStep;
    private String requestedBaseUrl;

    public Steps(RestfulStep restfulStep) {
        this.restfulStep = restfulStep;
    }

    @Given("base url {string}")
    public void base_url(String baseUrl) {
        CustomPicoFactory.lookupAction = s -> lookupAction(s, baseUrl);
        restfulStep.setBaseUrl(baseUrl);
    }

    @Given("response {int} on GET {string}:")
    public void responseOnGET(int code, String path, String body) {
        mockServer.when(request().withMethod("GET").withPath(path)).respond(response(body).withStatusCode(code));
    }

    @Then("{string} got a GET request on {string}")
    public void got_a_get_request_on(String url, String path) {
        mockServer.verify(request()
                        .withMethod("GET")
                        .withPath(path),
                VerificationTimes.once());
        assertThat(url).as(String.format("Expect %s to receive the request, but send to %s", url, requestedBaseUrl)).isEqualTo(requestedBaseUrl);
    }

    @SneakyThrows
    @Given("header by RESTful api:")
    public void header_by_res_tful_api(String headerJson) {
        new ObjectMapper().readValue(headerJson, new TypeReference<Map<String, Object>>() {
        }).forEach((key, value) -> {
            if (value instanceof String)
                restfulStep.header(key, (String) value);
            else
                restfulStep.header(key, (List<String>) value);
        });
    }

    @SneakyThrows
    @Then("got request:")
    public void got_request(String expression) {
        expect(new ObjectMapper().readValue(mockServer.retrieveRecordedRequests(request(), Format.JSON), List.class))
                .should(expression);
    }

    @After
    public void stopMockServer() {
        mockServer.reset();
    }

    @SneakyThrows
    private void lookupAction(@NotNull String s, String baseUrl) {
        assertThat(new URL(baseUrl).getHost()).isEqualTo(s);
        requestedBaseUrl = baseUrl;
    }
}
