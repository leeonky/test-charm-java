package com.github.leeonky.cucumber.restful;

import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

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
        okhttp3.Response rawResponse = httpClient.newCall(request.applyHeader(new okhttp3.Request.Builder()
                        .url(baseUrl + path))
                .build()).execute();
        response = new Response(rawResponse);
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