package org.testcharm.cucumber.restful;

import io.cucumber.docstring.DocString;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testcharm.dal.Accessors;
import org.testcharm.dal.DAL;
import org.testcharm.dal.extensions.basic.string.jsonsource.org.json.JSONArray;
import org.testcharm.dal.extensions.basic.string.jsonsource.org.json.JSONException;
import org.testcharm.dal.extensions.basic.string.jsonsource.org.json.JSONObject;
import org.testcharm.io.VirtualFile;
import org.testcharm.jfactory.JFactory;
import org.testcharm.jfactory.cucumber.Table;
import org.testcharm.util.BeanClass;
import org.testcharm.util.Collector;
import org.testcharm.util.PropertyReader;
import org.testcharm.util.Sneaky;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.testcharm.dal.Assertions.expect;
import static org.testcharm.dal.Evaluator.evaluate;
import static org.testcharm.dal.Evaluator.evaluateObject;
import static org.testcharm.dal.extensions.basic.binary.BinaryExtension.readAllAndClose;
import static org.testcharm.util.Sneaky.sneakyRun;

public class RestfulStep {
    public static final String CHARSET = "utf-8";
    private final Evaluator evaluator = new Evaluator();
    private String baseUrl = "";
    private Request request = new Request();
    private Response response;
    private HttpURLConnection connection;
    private Function<Object, String> serializer = body -> {
        String json = new JSONArray(Collections.singleton(body)).toString();
        return json.substring(1, json.length() - 1);
    };
    private JFactory jFactory;
    private String defautRequestContentType = "dal:application/json";
    private final List<ConnectionWriter> objectBodyWriters = asList(
            new ConnectionWriter() {
                @Override
                public boolean matches(String contentType) {
                    return contentType.equals("application/json");
                }

                @Override
                public void write(String contentType, String[] traitSpec, String bodyContent, HttpURLConnection connection) {
                    byte[] body = serializer.apply(parseBodyAndHeaders(bodyContent, traitSpec)).getBytes(UTF_8);
                    buildRequestBody(connection, contentType, body);
                }
            },
            new ConnectionWriter() {
                @Override
                public boolean matches(String contentType) {
                    return contentType.equals("multipart/form-data");
                }

                @Override
                public void write(String contentType, String[] traitSpec, String bodyContent, HttpURLConnection connection) {
                    Sneaky.run(() -> {
                        Object o = parseBodyAndHeaders(bodyContent, traitSpec);
                        connection.setDoOutput(true);
                        String boundary = UUID.randomUUID().toString();
                        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                        HttpStream httpStream = new HttpStream(connection.getOutputStream(), UTF_8);
                        DAL.dal().wrap(o).toMap().forEach((key, value) -> appendEntry(httpStream, key, value, boundary));
                        httpStream.close(boundary);
                    });
                }
            },
            new ConnectionWriter() {
                @Override
                public boolean matches(String contentType) {
                    return contentType.equals("application/octet-stream");
                }

                @Override
                public void write(String contentType, String[] traitSpec, String bodyContent, HttpURLConnection connection) {
                    Object o = parseOctetStreamBodyAndHeaders(bodyContent, traitSpec);
                    connection.setDoOutput(true);
                    if (o instanceof String) {
                        buildRequestBody(connection, contentType, ((String) o).getBytes());
                    } else if (o instanceof byte[]) {
                        buildRequestBody(connection, contentType, (byte[]) o);
                    } else if (o instanceof UploadFile) {
                        buildRequestBody(connection, contentType, ((UploadFile) o).getContent());
                    } else if (o instanceof VirtualFile) {
                        buildRequestBody(connection, contentType, ((VirtualFile) o).binary());
                    }
                }
            }
    );

    private final List<ConnectionWriter> textBodyWriters = asList(new ConnectionWriter() {
        @Override
        public boolean matches(String contentType) {
            return contentType.startsWith("multipart/form-data");
        }

        @Override
        public void write(String contentType, String[] traitSpec, String bodyContent, HttpURLConnection connection) {
            connection.setDoOutput(true);
            byte[] bytes = String.join("\r\n", bodyContent.split(System.lineSeparator())).getBytes();
            buildRequestBody(connection, contentType, bytes);
        }
    });

    private static Stream<String> getParamString(Map.Entry<String, Object> entry) {
        if (entry.getValue() instanceof List) {
            return ((List<?>) entry.getValue()).stream().map(value -> entry.getKey() + "[]=" + value);
        } else {
            return Stream.of(entry.getKey() + "=" + entry.getValue());
        }
    }

    public void setJFactory(JFactory jFactory) {
        this.jFactory = jFactory;
    }

    public void setSerializer(Function<Object, String> serializer) {
        this.serializer = serializer;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @When("GET {string}")
    public void get(String path) {
        requestAndResponse("GET", path, connection -> {
        });
    }

    @When("GET {string}:")
    public void getWithParams(String path, String params) {
        get(pathWithParams(path, params));
    }

    @When("DELETE {string}")
    public void delete(String path) {
        requestAndResponse("DELETE", path, connection -> {
        });
    }

    @When("DELETE {string}:")
    public void deleteWithParams(String path, String params) {
        delete(pathWithParams(path, params));
    }

    @When("POST {string}:")
    public void post(String path, DocString content) {
        requestBodyAndResponse("POST", path, null, content.getContentType(), content.getContent());
    }

    @When("POST {string} {string}:")
    @Then("POST {string} to {string}:")
    public void postWithSpec(String spec, String path, DocString body) {
        requestBodyAndResponse("POST", path, spec.split("[ ,]"), body.getContentType(), body.getContent());
    }

    @When("PUT {string}:")
    public void put(String path, DocString content) {
        requestBodyAndResponse("PUT", path, null, content.getContentType(), content.getContent());
    }

    @When("PUT {string} {string}:")
    @Then("PUT {string} to {string}:")
    public void putWithSpec(String spec, String path, DocString body) {
        requestBodyAndResponse("PUT", path, spec.split("[ ,]"), body.getContentType(), body.getContent());
    }

    @When("PATCH {string}:")
    public void patch(String path, DocString content) {
        requestBodyAndResponse("PATCH", path, null, content.getContentType(), content.getContent());
    }

    @When("PATCH {string} {string}:")
    @Then("PATCH {string} to {string}:")
    public void patchWithSpec(String spec, String path, DocString body) {
        requestBodyAndResponse("PATCH", path, spec.split("[ ,]"), body.getContentType(), body.getContent());
    }

    private void requestBodyAndResponse(String method, String path, String[] traitSpec, String contentType, String content) {
        String parsedContentType = processContentType(contentType, traitSpec);
        String bodyContent = evaluator.eval(content);
        if (traitSpec != null) {
            for (ConnectionWriter connectionWriter : objectBodyWriters) {
                if (connectionWriter.matches(parsedContentType)) {
                    requestAndResponse(method, path, connection -> connectionWriter.write(parsedContentType, traitSpec, bodyContent, connection));
                    return;
                }
            }
            requestAndResponse(method, path, connection1 -> buildRequestBody(connection1, parsedContentType, bodyContent.getBytes(UTF_8)));
        } else {
            if (parsedContentType.startsWith("dal:")) {
                for (ConnectionWriter connectionWriter : objectBodyWriters) {
                    if (connectionWriter.matches(parsedContentType.substring(4))) {
                        requestAndResponse(method, path, connection -> connectionWriter.write(parsedContentType.substring(4), traitSpec, bodyContent, connection));
                        return;
                    }
                }
                throw new IllegalStateException();
            } else {
                for (ConnectionWriter connectionWriter : textBodyWriters) {
                    if (connectionWriter.matches(parsedContentType)) {
                        requestAndResponse(method, path, connection -> connectionWriter.write(parsedContentType, traitSpec, bodyContent, connection));
                        return;
                    }
                }
                requestAndResponse(method, path, connection1 -> buildRequestBody(connection1, parsedContentType, bodyContent.getBytes(UTF_8)));
            }
        }
    }

    private void requestAndResponse(String method, String path, Consumer<HttpURLConnection> body) {
        Sneaky.run(() -> {
            URL url = new URL(baseUrl + evaluator.eval(path));
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            connection = request.applyHeader((HttpURLConnection) new URL(uri.toASCIIString()).openConnection());
            setRequestMethod(method);
            body.accept(connection);
            response = new Response(connection);
        });
    }

    private String processContentType(String contentType1, String[] traitSpec) {
        String contentType = contentType1;
        if (contentType == null || contentType.isEmpty())
            contentType = request.contentType();

        if (contentType == null || contentType.isEmpty())
            contentType = defautRequestContentType;
        if (traitSpec != null)
            contentType = contentType.replaceFirst("dal:", "");
        return contentType;
    }

    public void post(String path, byte[] bytes, String contentType) {
        requestAndResponse("POST", path, connection -> buildRequestBody(connection, contentType, bytes));
    }

    public void post(String path, String body, String contentType) {
        post(path, body.getBytes(UTF_8), contentType);
    }

    public void post(String path, String body) {
        post(path, body, null);
    }

    public void post(String path, Object body, String contentType) {
        post(path, serializer.apply(body), contentType);
    }

    public void post(String path, Object body) {
        post(path, body, null);
    }

    @When("POST form {string}:")
    public void postForm(String path, String form) {
        try {
            postForm(path, new JSONObject(evaluator.eval(form)).toMap());
        } catch (JSONException ig) {
            Collector collector = jFactory.collector();
            evaluateObject(form).on(collector);
            postForm(path, DAL.dal().wrap(collector.build()).toMap());
        }
    }

    public void postForm(String path, Map<String, ?> params) {
        requestAndResponse("POST", path, sneakyRun(connection -> {
            connection.setDoOutput(true);
            String boundary = UUID.randomUUID().toString();
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            HttpStream httpStream = new HttpStream(connection.getOutputStream(), UTF_8);
            params.forEach((key, value) -> appendEntry(httpStream, key, value, boundary));
            httpStream.close(boundary);
        }));
    }

    @When("POST form {string} {string}:")
    @Then("POST form {string} to {string}:")
    @SuppressWarnings("unchecked")
    public void postForm(String spec, String path, String form) {
        Map<String, Object>[] maps = Table.create(evaluator.eval(form)).flatSub();
        String[] delimiters = spec.split("[ ,]");
        Object formObject = jFactory.spec(delimiters).properties(maps[0]).create();
        BeanClass<Object> type = BeanClass.createFrom(formObject);
        postForm(path, new LinkedHashMap<String, Object>() {{
            type.getPropertyReaders().forEach((key, property) -> {
                Optional<PropertyReader<Object>> linkName = ((BeanClass<Object>) property.getType()).getPropertyReaders().values()
                        .stream().filter(p -> p.annotation(FormFileLinkName.class).isPresent()).findFirst();
                Object value = property.getValue(formObject);
                if (linkName.isPresent())
                    put("@" + key, linkName.get().getValue(value));
                else
                    put(key, value);
            });
        }});
    }

    public void put(String path, byte[] bytes, String contentType) {
        requestAndResponse("PUT", path, connection -> buildRequestBody(connection, contentType, bytes));
    }

    public void put(String path, String body, String contentType) {
        put(path, body.getBytes(UTF_8), contentType);
    }

    public void put(String path, String body) {
        put(path, body, null);
    }

    public void put(String path, Object body, String contentType) {
        put(path, serializer.apply(body), contentType);
    }

    public void put(String path, Object body) {
        put(path, body, null);
    }

    public void patch(String path, byte[] body, String contentType) {
        requestAndResponse("PATCH", path, connection -> buildRequestBody(connection, contentType, body));
    }

    public void patch(String path, String body, String contentType) {
        patch(path, body.getBytes(UTF_8), contentType);
    }

    public void patch(String path, String body) {
        patch(path, body, null);
    }

    public void patch(String path, Object body, String contentType) {
        patch(path, serializer.apply(body), contentType);
    }

    public void patch(String path, Object object) {
        patch(path, object, null);
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

    public <T> T response(String expression) {
        return Accessors.get(expression).from(response);
    }

    @Then("response should be:")
    public void responseShouldBe(String expression) {
        expect(response).should(expression);
    }

    @Then("data should be saved to {string} with response:")
    public void resourceShouldBe(String path, String expression) {
        responseShouldBe(expression);
        getAndResponseShouldBe(path, expression);
    }

    public void file(String fileKey, UploadFile file) {
        request.files.put(fileKey, file);
    }

    @Then("{string} should response:")
    public void getAndResponseShouldBe(String path, String expression) {
        get(path);
        responseShouldBe(expression);
    }

    @Then("DELETE {string} should response:")
    public void deleteAndResponseShouldBe(String path, String expression) {
        delete(path);
        responseShouldBe(expression);
    }

    private void appendEntry(HttpStream httpStream, String key, Object value, String boundary) {
        httpStream.bound(boundary, () -> {
            if (key.startsWith("@"))
                httpStream.appendFile(key, request.files.get(String.valueOf(value)));
            else if (value instanceof VirtualFile)
                httpStream.appendFile(key, ((VirtualFile) value).getName(), ((VirtualFile) value).binary());
            else
                httpStream.appendField(key, value);
        });
    }

    private void buildRequestBody(HttpURLConnection connection, String contentType, byte[] bytes) {
        Sneaky.run(() -> {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", contentType == null ? String.valueOf(request.headers.getOrDefault("Content-Type", "application/json")) : contentType);
            connection.getOutputStream().write(bytes);
            connection.getOutputStream().close();
        });
    }

    private String pathWithParams(String path, String params) {
        Object body = parseBodyAndHeaders(params, null);
        return path + "?" + DAL.dal().wrap(body).toMap().entrySet().stream()
                .flatMap(RestfulStep::getParamString).collect(joining("&"));
    }

    private Object parseBodyAndHeaders(String content, String[] traitSpec) {
        RequestCollector collector = new RequestCollector(jFactory, request.getContext());
        if (traitSpec != null)
            collector.traitsSpec(traitSpec);
        org.testcharm.dal.Evaluator.evaluateObject(content).on(collector);
        DAL.dal().wrap(collector.headerCollector().build()).toMap().forEach((key, value) -> {
            if (value instanceof Collection)
                header(String.valueOf(key), ((Collection<?>) value).stream().map(String::valueOf).collect(toList()));
            else
                header(String.valueOf(key), String.valueOf(value));

        });
        if (connection != null)
            request.applyHeader(connection);
        return collector.build();
    }

    private Object parseOctetStreamBodyAndHeaders(String content, String[] traitSpec) {
        RequestCollector collector = new RequestCollector(jFactory, request.getContext());
        if (traitSpec != null)
            collector.traitsSpec(traitSpec);
        Object on = evaluate(content).on(collector);
        DAL.dal().wrap(collector.headerCollector().build()).toMap().forEach((key, value) -> {
            if (value instanceof Collection)
                header(String.valueOf(key), ((Collection<?>) value).stream().map(String::valueOf).collect(toList()));
            else
                header(String.valueOf(key), String.valueOf(value));

        });
        if (connection != null)
            request.applyHeader(connection);
        if (on instanceof Collector) // use root collector
            return collector.build();
        return on;
    }

    private void setRequestMethod(String method) {
        Sneaky.run(() -> {
            if (method.equals("PATCH")) {
                Field field = getField(connection.getClass(), "method");
                field.setAccessible(true);
                field.set(connection, method);
            } else
                connection.setRequestMethod(method);
        });
    }

    private Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw new RuntimeException("Failed to get field " + fieldName + " from " + clazz, e);
            } else {
                return getField(superClass, fieldName);
            }
        }
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

    public static class Request {
        private final Map<String, UploadFile> files = new HashMap<>();
        private final Map<String, Object> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        public RequestContext getContext() {
            return new RequestContext();
        }

        public class RequestContext {
            public Map<String, UploadFile> getFiles() {
                return files;
            }
        }

        String contentType() {
            return (String) headers.get("Content-Type");
        }

        @SuppressWarnings("unchecked")
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
        public final int code;
        public final byte[] body;
        public final HttpURLConnection raw;

        public Response(HttpURLConnection connection) {
            raw = connection;
            code = Sneaky.get(connection::getResponseCode);
            InputStream stream = Sneaky.get(() -> 100 <= code && code <= 399 ? raw.getInputStream() : raw.getErrorStream());
            body = stream == null ? null : readAllAndClose(stream);
        }

        public Map<String, Object> getHeaders() {
            return raw.getHeaderFields().entrySet().stream()
                    .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue() != null && entry.getValue().size() == 1 ? entry.getValue().get(0) : entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        public String fileName() {
            String header = raw.getHeaderField("Content-Disposition");
            Matcher matcher = Pattern.compile(".*filename=\"(.*)\".*").matcher(header);
            return Sneaky.get(() -> URLDecoder.decode(matcher.matches() ? matcher.group(1) : header, UTF_8.name()));
        }
    }
}