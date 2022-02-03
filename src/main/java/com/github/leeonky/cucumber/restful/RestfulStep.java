package com.github.leeonky.cucumber.restful;

import io.cucumber.java.en.When;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class RestfulStep {
    private String baseUrl = null;
    private final OkHttpClient httpClient;

    public RestfulStep(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @When("GET {string}")
    public void get(String path) throws IOException {
        httpClient.newCall(request.applyHeader(new okhttp3.Request.Builder()
                .url(baseUrl + path))
                .build()).execute();
    }

    public RestfulStep header(String key, String value) {
        request.headers.put(key, value);
        return this;
    }

    public RestfulStep header(String key, Collection<String> value) {
        request.headers.put(key, value);
        return this;
    }

    private Request request = new Request();

    private static class Request {
        private final Map<String, Object> headers = new LinkedHashMap<>();

        @SuppressWarnings("unchecked")
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
}