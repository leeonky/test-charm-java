package com.github.leeonky.cucumber.restful;

import io.cucumber.java.en.When;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;

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
        httpClient.newCall(new Request.Builder()
                .url(baseUrl + path)
                .build()).execute();
    }
}