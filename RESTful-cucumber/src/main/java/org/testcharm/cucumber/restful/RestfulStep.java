package org.testcharm.cucumber.restful;

import io.cucumber.docstring.DocString;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testcharm.dal.Accessors;
import org.testcharm.dal.DAL;
import org.testcharm.dal.extensions.basic.string.jsonsource.org.json.JSONArray;
import org.testcharm.io.VirtualFile;
import org.testcharm.jfactory.JFactory;
import org.testcharm.util.BeanClass;
import org.testcharm.util.Collector;
import org.testcharm.util.PropertyReader;
import org.testcharm.util.Sneaky;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    public void setDefaultTextBodyWriter(TextBodyWriter defaultTextBodyWriter) {
        this.defaultTextBodyWriter = defaultTextBodyWriter;
    }

    private String defaultContentType = "dal:application/json";

    private final List<BodyRequestBuilder<ObjectBodyWriter>> objectBodyRequestBuilders = asList(
            new BodyRequestBuilder<ObjectBodyWriter>() {
                @Override
                public boolean matches(String contentType) {
                    return contentType.equals("application/json");
                }

                @Override
                public ObjectBodyWriter writer(String contentType) {
                    return (outputStream, object) ->
                            outputStream.write(serializer.apply(object).getBytes(UTF_8));
                }
            }, new BodyRequestBuilder<ObjectBodyWriter>() {
                @Override
                public boolean matches(String contentType) {
                    return contentType.equals("multipart/form-data");
                }

                @Override
                public ObjectBodyWriter writer(String contentType) {
                    String boundary = UUID.randomUUID().toString();
                    return new ObjectBodyWriter() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public void write(OutputStream outputStream, Object object) {
                            HttpStream httpStream = new HttpStream(outputStream, UTF_8);
                            if (object instanceof Map) {
                                DAL.dal().wrap(object).toMap().forEach((key, value) -> appendEntry(httpStream, key, value, boundary));
                            } else {
                                BeanClass<Object> type = BeanClass.createFrom(object);
                                DAL.dal().wrap(object).toMap().forEach((key, value) -> {
                                            Optional<PropertyReader<Object>> linkName = ((BeanClass<Object>) type.getPropertyReader(key).getType()).getPropertyReaders().values()
                                                    .stream().filter(p -> p.annotation(FormFileLinkName.class).isPresent()).findFirst();
                                            if (linkName.isPresent())
                                                appendEntry(httpStream, "@" + key, linkName.get().getValue(value), boundary);
                                            else
                                                appendEntry(httpStream, key, value, boundary);
                                        }
                                );
                            }
                            httpStream.close(boundary);
                        }

                        @Override
                        public String contentType(String contentType) {
                            return "multipart/form-data; boundary=" + boundary;
                        }
                    };
                }
            }, new BodyRequestBuilder<ObjectBodyWriter>() {
                @Override
                public boolean matches(String contentType) {
                    return contentType.equals("application/octet-stream");
                }

                @Override
                public ObjectBodyWriter writer(String contentType) {
                    return new ObjectBodyWriter() {
                        @Override
                        public Object body(Collector collector, Object result) {
                            return result instanceof Collector ? collector.build() : result;
                        }

                        @Override
                        public void write(OutputStream outputStream, Object object) throws IOException {
                            if (object instanceof String) {
                                outputStream.write(((String) object).getBytes());
                            } else if (object instanceof byte[]) {
                                outputStream.write((byte[]) object);
                            } else if (object instanceof UploadFile) {
                                outputStream.write(((UploadFile) object).getContent());
                            } else if (object instanceof VirtualFile) {
                                outputStream.write(((VirtualFile) object).binary());
                            }
                        }
                    };
                }
            }
    );
    private final List<BodyRequestBuilder<TextBodyWriter>> textBodyRequestBuilders = asList(
            new BodyRequestBuilder<TextBodyWriter>() {
                @Override
                public boolean matches(String contentType) {
                    return contentType.startsWith("multipart/form-data");
                }

                @Override
                public TextBodyWriter writer(String contentType) {
                    return (outputStream, content) -> outputStream.write(String.join("\r\n", content.split(System.lineSeparator())).getBytes());
                }
            }
    );

    private TextBodyWriter defaultTextBodyWriter = (outputStream, content) -> outputStream.write(content.getBytes());

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
        requestBodyAndResponse("POST", path, content.getContentType(), content.getContent());
    }

    private void requestBodyAndResponse(String method, String path, String contentType, String content) {
        if (contentType == null)
            contentType = request.contentType();
        if (contentType == null)
            contentType = defaultContentType;
        if (contentType.startsWith("dal:")) {
            RequestCollector collector = new RequestCollector(jFactory, request.getContext());
            Object result = org.testcharm.dal.Evaluator.evaluateObject(evaluator.eval(content)).on(collector);
            applyCollectedHeaders(collector);
            writeObjectBody(method, path, contentType.substring(4), objectBodyWriter -> objectBodyWriter.body(collector, result));
        } else {
            String resolvedContentType = contentType;
            requestAndResponse(method, path, sneakyRun(connection1 -> {
                TextBodyWriter textBodyWriter = textBodyRequestBuilders.stream().filter(builder -> builder.matches(resolvedContentType))
                        .map(builder -> builder.writer(resolvedContentType))
                        .findFirst().orElse(defaultTextBodyWriter);
                connection1.setDoOutput(true);
                connection1.setRequestProperty("Content-Type", textBodyWriter.contentType(resolvedContentType));
                textBodyWriter.write(connection1.getOutputStream(), evaluator.eval(content));
                connection1.getOutputStream().close();
            }));
        }
    }

    private void requestBodyAndResponse(String method, String path, String contentType, String content, String[] traitSpec) {
        RequestCollector collector = new RequestCollector(jFactory, request.getContext());
        collector.traitsSpec(traitSpec);
        Object result = org.testcharm.dal.Evaluator.evaluateObject(evaluator.eval(content)).on(collector);
        applyCollectedHeaders(collector);
        writeObjectBody(method, path, resolveObjectContentType(contentType), objectBodyWriter -> objectBodyWriter.body(collector, result));
    }

    private void requestBodyAndResponse(String method, String path, String contentType, Object body) {
        writeObjectBody(method, path, resolveObjectContentType(contentType), objectBodyWriter -> body);
    }

    private String resolveObjectContentType(String contentType) {
        if (contentType == null)
            contentType = request.contentType();
        if (contentType == null)
            contentType = defaultContentType.replaceFirst("^dal:", "");
        return contentType;
    }

    private void applyCollectedHeaders(RequestCollector collector) {
        DAL.dal().wrap(collector.headerCollector().build()).toMap().forEach((key, value) -> {
            if (value instanceof Collection)
                header(String.valueOf(key), ((Collection<?>) value).stream().map(String::valueOf).collect(toList()));
            else
                header(String.valueOf(key), String.valueOf(value));
        });
    }

    private void writeObjectBody(String method, String path, String resolvedContentType,
                                 Function<ObjectBodyWriter, Object> bodyResolver) {
        requestAndResponse(method, path, sneakyRun(connection -> {
            ObjectBodyWriter objectBodyWriter = objectBodyRequestBuilders.stream().filter(builder -> builder.matches(resolvedContentType))
                    .map(builder -> builder.writer(resolvedContentType)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown object writer for content type: %s", resolvedContentType)));
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", objectBodyWriter.contentType(resolvedContentType));
            objectBodyWriter.write(connection.getOutputStream(), bodyResolver.apply(objectBodyWriter));
            connection.getOutputStream().close();
        }));
    }

    @When("POST {string} {string}:")
    @Then("POST {string} to {string}:")
    public void postWithSpec(String spec, String path, DocString body) {
        requestBodyAndResponse("POST", path, body.getContentType(), body.getContent(), spec.split("[ ,]"));
    }

    @When("PUT {string}:")
    public void put(String path, DocString content) {
        requestBodyAndResponse("PUT", path, content.getContentType(), content.getContent());
    }

    @When("PUT {string} {string}:")
    @Then("PUT {string} to {string}:")
    public void putWithSpec(String spec, String path, DocString body) {
        requestBodyAndResponse("PUT", path, body.getContentType(), body.getContent(), spec.split("[ ,]"));
    }

    @When("PATCH {string}:")
    public void patch(String path, DocString content) {
        requestBodyAndResponse("PATCH", path, content.getContentType(), content.getContent());
    }

    @When("PATCH {string} {string}:")
    @Then("PATCH {string} to {string}:")
    public void patchWithSpec(String spec, String path, DocString body) {
        requestBodyAndResponse("PATCH", path, body.getContentType(), body.getContent(), spec.split("[ ,]"));
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

    public void post(String path, String contentType, String content) {
        requestBodyAndResponse("POST", path, contentType, content);
    }

    public void post(String path, String content) {
        post(path, null, content);
    }

    public void post(String path, String contentType, Object body) {
        requestBodyAndResponse("POST", path, contentType, body);
    }

    public void post(String path, Object body) {
        post(path, null, body);
    }

    @When("POST form {string}:")
    public void postForm(String path, String form) {
        requestBodyAndResponse("POST", path, "dal:multipart/form-data", form);
    }

    @When("POST form {string} {string}:")
    @Then("POST form {string} to {string}:")
    public void postForm(String spec, String path, String form) {
        requestBodyAndResponse("POST", path, "multipart/form-data", form, spec.split("[ ,]"));
    }

    public void put(String path, String contentType, String content) {
        requestBodyAndResponse("PUT", path, contentType, content);
    }

    public void put(String path, String content) {
        put(path, null, content);
    }

    public void put(String path, String contentType, Object body) {
        requestBodyAndResponse("PUT", path, contentType, body);
    }

    public void put(String path, Object body) {
        put(path, null, body);
    }

    public void patch(String path, String contentType, String content) {
        requestBodyAndResponse("PATCH", path, contentType, content);
    }

    public void patch(String path, String content) {
        patch(path, null, content);
    }

    public void patch(String path, String contentType, Object object) {
        requestBodyAndResponse("PATCH", path, contentType, object);
    }

    public void patch(String path, Object object) {
        patch(path, null, object);
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

    private String pathWithParams(String path, String params) {
        RequestCollector collector = new RequestCollector(jFactory, request.getContext());
        org.testcharm.dal.Evaluator.evaluateObject(params).on(collector);
        applyCollectedHeaders(collector);
        Object body = collector.build();
        return path + "?" + DAL.dal().wrap(body).toMap().entrySet().stream()
                .flatMap(RestfulStep::getParamString).collect(joining("&"));
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