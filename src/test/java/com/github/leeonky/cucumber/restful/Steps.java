package com.github.leeonky.cucumber.restful;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.leeonky.cucumber.restful.extensions.PathVariableReplacement;
import com.github.leeonky.dal.DAL;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;
import org.apache.commons.fileupload.MultipartStream;
import org.jetbrains.annotations.NotNull;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Format;
import org.mockserver.verify.VerificationTimes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.leeonky.cucumber.restful.RestfulStep.UploadFile.content;
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

    @Given("response {int} on {string} {string}:")
    public void responseOnGET(int code, String method, String path, String body) {
        mockServer.when(request().withMethod(method).withPath(path)).respond(response(body).withStatusCode(code));
    }

    @Given("binary response {int} on GET {string}:")
    public void binary_response_on_get(Integer code, String path, String body) {
        mockServer.when(request().withMethod("GET").withPath(path))
                .respond(response().withBody(body.getBytes(StandardCharsets.UTF_8)).withStatusCode(code));
    }

    @Then("{string} got a {string} request on {string}")
    public void got_a_get_request_on(String url, String method, String path) {
        mockServer.verify(request()
                        .withMethod(method)
                        .withPath(path),
                VerificationTimes.once());
        assertThat(url).as(String.format("Expect %s to receive the request, but send to %s", url, requestedBaseUrl)).isEqualTo(requestedBaseUrl);
    }

    @Then("{string} got a {string} request on {string} with body")
    public void got_a_request_on_with_body(String url, String method, String path, String body) {
        mockServer.verify(request()
                        .withMethod(method)
                        .withPath(path)
                        .withBody(body),
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
        String content = mockServer.retrieveRecordedRequests(request(), Format.JSON);
        System.out.println("content = " + content);
        expect(new ObjectMapper().readValue(content, List.class)).should(expression);
    }

    @After
    public void stopMockServer() {
        mockServer.reset();
    }

    @Given("var {string} value is {string}")
    public void varValueIs(String varName, String value) {
        PathVariableReplacement.replacements.put(varName, value);
        PathVariableReplacement.evaluator = s -> PathVariableReplacement.replacements.get(s);
    }

    @Given("a file {string}:")
    public void a_file(String fileKey, String fileContent) {
        restfulStep.file(fileKey, content(fileContent));
    }

    @Given("a file {string} with name {string}:")
    public void a_file_with_name(String fileKey, String fileName, String fileContent) {
        restfulStep.file(fileKey, content(fileContent).name(fileName));
    }

    @SneakyThrows
    @Then("got request form value:")
    public void got_request_form_value(String expression) {
        List actual = new ObjectMapper().readValue(mockServer.retrieveRecordedRequests(request(), Format.JSON), List.class);
        String string = new DAL().evaluate(actual, "[0].body.string").toString();
        String substring = string.substring(2, string.indexOf('\r'));
        MultipartStream multipartStream = new MultipartStream(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)),
                substring.getBytes(StandardCharsets.UTF_8));

        List<Map<String, String>> bodyHeaders = new ArrayList<>();
        boolean nextPart = multipartStream.skipPreamble();
        while (nextPart) {
            bodyHeaders.add(new HashMap<String, String>() {{
                put("headers", multipartStream.readHeaders().trim());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                multipartStream.readBodyData(stream);
                put("body", stream.toString().trim());
            }});
            nextPart = multipartStream.readBoundary();
        }
        expect(bodyHeaders)
                .should(expression);
    }

    @Before
    public void noReplacement() {
        PathVariableReplacement.reset();
    }

    @SneakyThrows
    private void lookupAction(@NotNull String s, String baseUrl) {
        assertThat(new URL(baseUrl).getHost()).isEqualTo(s);
        requestedBaseUrl = baseUrl;
    }
}
