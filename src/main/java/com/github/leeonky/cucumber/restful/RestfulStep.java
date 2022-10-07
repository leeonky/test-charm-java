package com.github.leeonky.cucumber.restful;

import com.github.leeonky.util.Suppressor;
import io.cucumber.docstring.DocString;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.leeonky.dal.Assertions.expect;
import static com.github.leeonky.dal.extensions.BinaryExtension.readAllAndClose;
import static java.nio.charset.StandardCharsets.UTF_8;

public class RestfulStep {
    public static final String CHARSET = "utf-8";
    private final Evaluator evaluator = new Evaluator();
    private String baseUrl = null;
    private Request request = new Request();
    private Response response;
    private HttpURLConnection connection;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @When("GET {string}")
    public void get(String path) throws IOException, URISyntaxException {
        requestAndResponse("GET", path, connection -> {
        });
    }

    @When("POST {string}:")
    public void post(String path, DocString content) throws IOException, URISyntaxException {
        post(path, evaluator.eval(content.getContent()), content.getContentType());
    }

    public void post(String path, String body, String contentType) throws IOException, URISyntaxException {
        requestAndResponse("POST", path, connection -> buildRequestBody(connection, contentType, body));
    }

    public void post(String path, String body) throws IOException, URISyntaxException {
        post(path, body, null);
    }

    public void post(String path, Object body, String contentType) throws IOException, URISyntaxException {
        post(path, toJson(body), contentType);
    }

    public static String toJson(Object body) {
        String json = new JSONArray(Collections.singleton(body)).toString();
        return json.substring(1, json.length() - 1);
    }

    public void post(String path, Object body) throws IOException, URISyntaxException {
        post(path, body, null);
    }

    @When("POST form {string}:")
    public void postForm(String path, String form) throws IOException, URISyntaxException {
        postForm(path, new JSONObject(evaluator.eval(form)).toMap());
    }

    public void postForm(String path, Map<String, ?> params) throws IOException, URISyntaxException {
        requestAndResponse("POST", path, connection -> Suppressor.run(() -> {
            connection.setDoOutput(true);
            String boundary = UUID.randomUUID().toString();
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            HttpStream httpStream = new HttpStream(connection.getOutputStream(), UTF_8);
            params.forEach((key, value) -> appendEntry(httpStream, key, value == null ? null : value.toString(), boundary));
            httpStream.close(boundary);
        }));
    }

    @When("PUT {string}:")
    public void put(String path, DocString content) throws IOException, URISyntaxException {
        put(path, evaluator.eval(content.getContent()), content.getContentType());
    }

    public void put(String path, String body, String contentType) throws IOException, URISyntaxException {
        requestAndResponse("PUT", path, connection -> buildRequestBody(connection, contentType, body));
    }

    public void put(String path, String body) throws IOException, URISyntaxException {
        put(path, body, null);
    }

    public void put(String path, Object body, String contentType) throws IOException, URISyntaxException {
        put(path, toJson(body), contentType);
    }

    public void put(String path, Object body) throws IOException, URISyntaxException {
        put(path, body, null);
    }

    @When("DELETE {string}")
    public void delete(String path) throws IOException, URISyntaxException {
        requestAndResponse("DELETE", path, connection -> {
        });
    }

    @After
    public void reset() {
        request = new Request();
        response = null;
        connection = null;
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
    public <T> T responseShouldBe(String expression) {
        return expect(response).should(System.lineSeparator() + expression);
    }

    @Then("data should be saved to {string} with response:")
    public <T> T resourceShouldBe(String path, String expression) throws IOException, URISyntaxException {
        responseShouldBe(expression);
        return getAndResponseShouldBe(path, expression);
    }

    public void file(String fileKey, UploadFile file) {
        request.files.put(fileKey, file);
    }

    @Then("{string} should response:")
    public <T> T getAndResponseShouldBe(String path, String expression) throws IOException, URISyntaxException {
        get(path);
        return responseShouldBe(expression);
    }

    private void appendEntry(HttpStream httpStream, String key, String value, String boundary) {
        httpStream.bound(boundary, () -> Suppressor.get(() -> key.startsWith("@") ?
                httpStream.appendFile(key, request.files.get(value)) : httpStream.appendField(key, value)));
    }

    private void buildRequestBody(HttpURLConnection connection, String contentType, String body) {
        Suppressor.run(() -> {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", contentType == null ? String.valueOf(request.headers.getOrDefault("Content-Type", "application/json")) : contentType);
            connection.getOutputStream().write(body.getBytes(UTF_8));
            connection.getOutputStream().close();
        });
    }

    private void requestAndResponse(String method, String path, Consumer<HttpURLConnection> body) throws IOException, URISyntaxException {
        URL url = new URL(baseUrl + evaluator.eval(path));
        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
        connection = request.applyHeader((HttpURLConnection) new URL(uri.toASCIIString()).openConnection());
        connection.setRequestMethod(method);
        body.accept(connection);
        response = new Response(connection);
    }

    public interface UploadFile {
        static UploadFile content(String fileContent) {
            return content(fileContent.getBytes(UTF_8));
        }

        static UploadFile content(byte[] bytes) {
            return () -> bytes;
        }

        byte[] getContent();

        default String getName() {
            return UUID.randomUUID() + ".upload";
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
        private final Map<String, UploadFile> files = new HashMap<>();
        private final Map<String, Object> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        private HttpURLConnection applyHeader(HttpURLConnection connection) {
            headers.forEach((key, value) -> {
                if (value instanceof String)
                    connection.setRequestProperty(key, (String) value);
                else if (value instanceof Collection)
                    ((Collection<String>) value).forEach(header -> connection.addRequestProperty(key, header));
            });
            return connection;
        }
    }

    public static class Response {
        public final HttpURLConnection raw;
        public final int code;
        public final byte[] body;

        public Response(HttpURLConnection connection) {
            raw = connection;
            code = Suppressor.get(connection::getResponseCode);
            InputStream stream = Suppressor.get(() -> 100 <= code && code <= 399 ? raw.getInputStream() : raw.getErrorStream());
            body = stream == null ? null : readAllAndClose(stream);
        }

        public Map<String, Object> headers() {
            return raw.getHeaderFields().entrySet().stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue() != null && entry.getValue().size() == 1 ? entry.getValue().get(0) : entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        public String fileName() {
            String header = raw.getHeaderField("Content-Disposition");
            Matcher matcher = Pattern.compile(".*filename=\"(.*)\".*").matcher(header);
            return Suppressor.get(() -> URLDecoder.decode(matcher.matches() ? matcher.group(1) : header, UTF_8.name()));
        }
    }
}