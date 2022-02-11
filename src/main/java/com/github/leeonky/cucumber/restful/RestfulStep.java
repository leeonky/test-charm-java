package com.github.leeonky.cucumber.restful;

import io.cucumber.docstring.DocString;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.github.leeonky.dal.extension.assertj.DALAssert.expect;

public class RestfulStep {
    private final OkHttpClient httpClient;
    private String baseUrl = null;
    private Request request = new Request();
    private Response response;

    public RestfulStep(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @When("GET {string}")
    public void get(String path) throws IOException {
        requestAndResponse(path, okhttp3.Request.Builder::get);
    }

    @When("POST {string}:")
    public void post(String path, DocString content) throws IOException {
        requestAndResponse(path, builder -> builder.post(createRequestBody(content, builder)));
    }

    @When("PUT {string}:")
    public void put(String path, DocString content) throws IOException {
        requestAndResponse(path, builder -> builder.put(createRequestBody(content, builder)));
    }

    @When("DELETE {string}")
    public void delete(String path) throws IOException {
        requestAndResponse(path, okhttp3.Request.Builder::delete);
    }

    @After
    public void reset() {
        request = new Request();
        response = null;
    }

    public RestfulStep header(String key, String value) {
        request.headers.put(key, value);
        return this;
    }

    public RestfulStep header(String key, Collection<String> value) {

        request.headers.put(key, value);
        return this;
    }

    @Then("response should be:")
    public void responseShouldBe(String expression) {
        expect(response).should(expression);
    }

    @NotNull
    private RequestBody createRequestBody(DocString docString, okhttp3.Request.Builder builder) {
        String contentType = docString.getContentType() == null ? "application/json" : docString.getContentType();
        builder.addHeader("Content-Type", contentType);
        return RequestBody.create(docString.getContent().getBytes(StandardCharsets.UTF_8));
    }

    private void requestAndResponse(String path, UnaryOperator<okhttp3.Request.Builder> action) throws IOException {
        okhttp3.Request.Builder builder = request.applyHeader(new okhttp3.Request.Builder()
                .url(baseUrl + path));
        okhttp3.Response rawResponse = httpClient.newCall(action.apply(builder).build()).execute();
        response = new Response(rawResponse);
    }

    private static class Request {
        private final Map<String, Object> headers = new LinkedHashMap<>();

        private okhttp3.Request.Builder applyHeader(okhttp3.Request.Builder builder) {
            headers.forEach((key, value) -> {
                if (value instanceof String)
                    builder.header(key, (String) value);
                else
                    ((Collection<String>) value).forEach(header -> builder.addHeader(key, header));
            });
            return builder;
        }
    }

    public static class Response {

        public final okhttp3.Response raw;

        public Response(okhttp3.Response raw) {
            this.raw = raw;
        }

        public int code() {
            return raw.code();
        }

        public byte[] body() throws IOException {
            return raw.body().bytes();
        }
    }

}