package com.github.leeonky.cucumber.restful;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.docstring.DocString;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.leeonky.dal.extension.assertj.DALAssert.expect;
import static okhttp3.MediaType.parse;

public class RestfulStep {
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Evaluator evaluator = new Evaluator();
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
        requestAndResponse(path, Builder::get);
    }

    @When("POST {string}:")
    public void post(String path, DocString content) throws IOException {
        requestAndResponse(path, builder -> builder.post(createRequestBody(content, builder)));
    }

    @When("POST form {string}:")
    public void postForm(String path, String form) throws IOException {
        requestAndResponse(path, builder -> {
            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(parse("multipart/form-data"));
            try {
                objectMapper.readValue(form, new TypeReference<Map<String, String>>() {
                }).forEach((key, value) -> {
                    addFormPart(bodyBuilder, evaluator.eval(key), evaluator.eval(value));
                });
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return builder.post(bodyBuilder.build());
        });
    }

    @When("PUT {string}:")
    public void put(String path, DocString content) throws IOException {
        requestAndResponse(path, builder -> builder.put(createRequestBody(content, builder)));
    }

    @When("DELETE {string}")
    public void delete(String path) throws IOException {
        requestAndResponse(path, Builder::delete);
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

    public void file(String fileKey, UploadFile file) {
        request.files.put(fileKey, file);
    }

    private void addFormPart(MultipartBody.Builder bodyBuilder, String key, String value) {
        if (key.startsWith("@"))
            appendFile(bodyBuilder, key, value);
        else
            bodyBuilder.addFormDataPart(key, value);
    }

    private void appendFile(MultipartBody.Builder bodyBuilder, String key, String value) {
        UploadFile uploadFile = request.files.get(value);
        bodyBuilder.addFormDataPart(key.substring(1), uploadFile.getName(),
                RequestBody.create(uploadFile.getContent()));
    }

    private RequestBody createRequestBody(DocString docString, Builder builder) {
        String contentType = docString.getContentType() == null ? "application/json" : docString.getContentType();
        builder.addHeader("Content-Type", contentType);
        return RequestBody.create(evaluator.eval(docString.getContent()).getBytes(StandardCharsets.UTF_8));
    }

    private void requestAndResponse(String path, UnaryOperator<Builder> action) throws IOException {
        Builder builder = request.applyHeader(new Builder().url(baseUrl + evaluator.eval(path)));
        okhttp3.Response rawResponse = httpClient.newCall(action.apply(builder).build()).execute();
        response = new Response(rawResponse);
    }

    public interface UploadFile {
        static UploadFile content(String fileContent) {
            return () -> fileContent.getBytes(StandardCharsets.UTF_8);
        }

        byte[] getContent();

        default String getName() {
            return Instant.now().toEpochMilli() + ".upload";
        }

        default UploadFile name(String fileName) {
            return new UploadFile() {
                @Override
                public byte[] getContent() {
                    return UploadFile.this.getContent();
                }

                @Override
                public String getName() {
                    return fileName;
                }
            };
        }
    }

    private static class Request {
        public final Map<String, UploadFile> files = new HashMap<>();
        private final Map<String, Object> headers = new LinkedHashMap<>();

        private Builder applyHeader(Builder builder) {
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

        public String fileName() {
            String header = raw.header("Content-Disposition");
            Matcher matcher = Pattern.compile(".*filename=\"(.*)\".*").matcher(header);
            return matcher.matches() ? matcher.group(1) : header;
        }
    }
}