package com.github.leeonky.cucumber.restful;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.leeonky.util.Suppressor;
import io.cucumber.docstring.DocString;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.leeonky.dal.Assertions.expect;

public class RestfulStep {
    public static final String CHARSET = "utf-8";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Evaluator evaluator = new Evaluator();
    private String baseUrl = null;
    private Request request = new Request();
    private Response response;
    private HttpURLConnection connection;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @When("GET {string}")
    public void get(String path) throws IOException {
        requestAndResponse("GET", path, connection -> {
        });
    }

    @When("POST {string}:")
    public void post(String path, DocString content) throws IOException {
        requestAndResponse("POST", path, connection -> buildRequestBody(content, connection));
    }

    @When("POST form {string}:")
    public void postForm(String path, String form) throws IOException {
        requestAndResponse("POST", path, connection -> Suppressor.run(() -> {
            connection.setDoOutput(true);
            String boundary = UUID.randomUUID().toString();
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET), true);
            String LINE_FEED = "\r\n";
            objectMapper.readValue(evaluator.eval(form), new TypeReference<Map<String, String>>() {
            }).forEach((key, value) -> {
                if (key.startsWith("@")) {
                    UploadFile uploadFile = request.files.get(value);
                    String fileName = uploadFile.getName();
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"" + key.substring(1) + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
                    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
                    writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                    writer.append(LINE_FEED).flush();
                    Suppressor.run(() -> outputStream.write(uploadFile.getContent()));
                    Suppressor.run(outputStream::flush);
                    writer.append(LINE_FEED);
                } else {
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"" + key + "\"").append(LINE_FEED);
                    writer.append("Content-Type: text/plain; charset=" + CHARSET).append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.append(value).append(LINE_FEED);
                }
            });
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();
        }));
    }

    @When("PUT {string}:")
    public void put(String path, DocString content) throws IOException {
        requestAndResponse("PUT", path, connection -> buildRequestBody(content, connection));
    }

    private void buildRequestBody(DocString content, HttpURLConnection connection) {
        Suppressor.run(() -> {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", content.getContentType() == null ? "application/json"
                    : content.getContentType());
            connection.getOutputStream().write(evaluator.eval(content.getContent()).getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().close();
        });
    }

    private void requestAndResponse(String method, String path, Consumer<HttpURLConnection> body) throws IOException {
        connection = request.applyHeader((HttpURLConnection) new URL(baseUrl + evaluator.eval(path)).openConnection());
        connection.setRequestMethod(method);
        body.accept(connection);
        response = new Response(connection);
    }

    @When("DELETE {string}")
    public void delete(String path) throws IOException {
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
    public void responseShouldBe(String expression) {
        expect(response).should(System.lineSeparator() + expression);
    }

    public void file(String fileKey, UploadFile file) {
        request.files.put(fileKey, file);
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
        private final Map<String, UploadFile> files = new HashMap<>();
        private final Map<String, Object> headers = new LinkedHashMap<>();

        private HttpURLConnection applyHeader(HttpURLConnection connection) {
            headers.forEach((key, value) -> {
                if (value instanceof String)
                    connection.setRequestProperty(key, (String) value);
                else
                    ((Collection<String>) value).forEach(header -> connection.addRequestProperty(key, header));
            });
            return connection;
        }
    }

    public static class Response {
        public final HttpURLConnection raw;
        private final int code;

        public Response(HttpURLConnection connection) {
            raw = connection;
            code = Suppressor.get(connection::getResponseCode);
        }

        public int code() {
            return code;
        }

        public InputStream body() {
            return Suppressor.get(() -> 100 <= code && code <= 399 ? raw.getInputStream() : raw.getErrorStream());
        }

        public String fileName() {
            String header = raw.getHeaderField("Content-Disposition");
            Matcher matcher = Pattern.compile(".*filename=\"(.*)\".*").matcher(header);
            return matcher.matches() ? matcher.group(1) : header;
        }
    }
}