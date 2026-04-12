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
import org.testcharm.util.PropertyReader;
import org.testcharm.util.Sneaky;

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
import static java.util.Collections.singletonList;
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

    public void setDefaultDocType(String defaultDocType) {
        this.defaultDocType = defaultDocType;
    }

    public void setDefaultTextBodyWriter(TextBodyWriter defaultTextBodyWriter) {
        this.defaultTextBodyWriter = defaultTextBodyWriter;
    }

    private String defaultDocType = "application/json";

    private final LinkedList<BodyRequestBuilder<ObjectBodyWriter>> objectBodyRequestBuilders = new LinkedList<>(asList(
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
                    return (outputStream, object) -> {
                        if (object instanceof String)
                            outputStream.write(((String) object).getBytes());
                        else if (object instanceof byte[])
                            outputStream.write((byte[]) object);
                        else if (object instanceof UploadFile)
                            outputStream.write(((UploadFile) object).getContent());
                        else if (object instanceof VirtualFile)
                            outputStream.write(((VirtualFile) object).binary());
                    };
                }
            }
    ));

    public void addObjectBodyRequestBuilder(BodyRequestBuilder<ObjectBodyWriter> builder) {
        objectBodyRequestBuilders.addFirst(builder);
    }

    private final LinkedList<BodyRequestBuilder<TextBodyWriter>> textBodyRequestBuilders = new LinkedList<>(singletonList(
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
    ));

    public void addTextBodyRequestBuilder(BodyRequestBuilder<TextBodyWriter> builder) {
        textBodyRequestBuilders.addFirst(builder);
    }

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
    public void post(String path, DocString contentOrExpression) {
        post(path, contentOrExpression.getContentType(), contentOrExpression.getContent());
    }

    @When("POST form {string}:")
    public void postForm(String path, String expression) {
        requestBodyAndResponse("POST", path, "dal:multipart/form-data", expression);
    }

    @When("POST form {string} {string}:")
    @Then("POST form {string} to {string}:")
    public void postForm(String spec, String path, String expression) {
        postWithSpec(path, "multipart/form-data", spec.split("[ ,]"), expression);
    }

    @When("POST {string} {string}:")
    @Then("POST {string} to {string}:")
    public void postWithSpec(String spec, String path, DocString expression) {
        postWithSpec(path, expression.getContentType(), spec.split("[ ,]"), expression.getContent());
    }

    public void postWithSpec(String path, String contentType, String[] traitSpec, String expression) {
        requestBodyAndResponse("POST", path, contentType, expression, traitSpec);
    }

    public void post(String path, String docType, String contentOrExpression) {
        requestBodyAndResponse("POST", path, docType, contentOrExpression);
    }

    public void postInDefault(String path, String contentOrExpression) {
        post(path, null, contentOrExpression);
    }

    public void postObject(String path, String contentType, Object body) {
        requestObjectBodyAndResponse("POST", path, contentType, body);
    }

    public void postObjectInDefault(String path, Object body) {
        postObject(path, null, body);
    }

    @When("PUT {string}:")
    public void put(String path, DocString contentOrExpression) {
        put(path, contentOrExpression.getContentType(), contentOrExpression.getContent());
    }

    @When("PUT {string} {string}:")
    @Then("PUT {string} to {string}:")
    public void putWithSpec(String spec, String path, DocString expression) {
        putWithSpec(path, expression.getContentType(), spec.split("[ ,]"), expression.getContent());
    }

    public void putWithSpec(String path, String contentType, String[] traitSpec, String expression) {
        requestBodyAndResponse("PUT", path, contentType, expression, traitSpec);
    }

    public void put(String path, String docType, String contentOrExpression) {
        requestBodyAndResponse("PUT", path, docType, contentOrExpression);
    }

    public void putInDefault(String path, String contentOrExpression) {
        put(path, null, contentOrExpression);
    }

    public void putObject(String path, String contentType, Object body) {
        requestObjectBodyAndResponse("PUT", path, contentType, body);
    }

    public void putObjectInDefault(String path, Object body) {
        putObject(path, null, body);
    }

    @When("PATCH {string}:")
    public void patch(String path, DocString contentOrExpression) {
        patch(path, contentOrExpression.getContentType(), contentOrExpression.getContent());
    }

    @When("PATCH {string} {string}:")
    @Then("PATCH {string} to {string}:")
    public void patchWithSpec(String spec, String path, DocString expression) {
        patchWithSpec(path, expression.getContentType(), spec.split("[ ,]"), expression.getContent());
    }

    public void patchWithSpec(String path, String contentType, String[] traitSpec, String expression) {
        requestBodyAndResponse("PATCH", path, contentType, expression, traitSpec);
    }

    public void patch(String path, String docType, String contentOrExpression) {
        requestBodyAndResponse("PATCH", path, docType, contentOrExpression);
    }

    public void patchInDefault(String path, String contentOrExpression) {
        patch(path, null, contentOrExpression);
    }

    public void patchObject(String path, String contentType, Object object) {
        requestObjectBodyAndResponse("PATCH", path, contentType, object);
    }

    public void patchObjectInDefault(String path, Object object) {
        patchObject(path, null, object);
    }


    private void requestBodyAndResponse(String method, String path, String docType, String content) {
        if ("dal".equals(docType)) {
            RequestCollector collector = new RequestCollector(jFactory, request.getContext());
            Object result = org.testcharm.dal.Evaluator.evaluateObject(evaluator.eval(content)).on(collector);
            applyCollectedHeaders(collector);
            if (request.contentType() != null)
                docType = "dal:" + request.contentType();
            else
                docType = "dal:" + defaultDocType.replaceFirst("^dal:", "");
            writeObjectBody(method, path, docType.substring(4), objectBodyWriter -> objectBodyWriter.body(collector, result));
        } else {
            if (docType == null)
                docType = request.contentType();
            if (docType == null)
                docType = defaultDocType;
            if (docType.startsWith("dal:")) {
                RequestCollector collector = new RequestCollector(jFactory, request.getContext());
                Object result = org.testcharm.dal.Evaluator.evaluateObject(evaluator.eval(content)).on(collector);
                applyCollectedHeaders(collector);
                writeObjectBody(method, path, docType.substring(4), objectBodyWriter -> objectBodyWriter.body(collector, result));
            } else {
                String resolvedContentType = docType;
                requestAndResponse(method, path, sneakyRun(connection -> {
                    TextBodyWriter textBodyWriter = textBodyRequestBuilders.stream().filter(builder -> builder.matches(resolvedContentType))
                            .map(builder -> builder.writer(resolvedContentType))
                            .findFirst().orElse(defaultTextBodyWriter);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", textBodyWriter.contentType(resolvedContentType));
                    textBodyWriter.write(connection.getOutputStream(), evaluator.eval(content));
                    connection.getOutputStream().close();
                }));
            }
        }
    }

    private void requestBodyAndResponse(String method, String path, String docType, String content, String[] traitSpec) {
        RequestCollector collector = new RequestCollector(jFactory, request.getContext());
        collector.traitsSpec(traitSpec);
        Object result = org.testcharm.dal.Evaluator.evaluateObject(evaluator.eval(content)).on(collector);
        applyCollectedHeaders(collector);
        writeObjectBody(method, path, resolveObjectContentType(docType), objectBodyWriter -> objectBodyWriter.body(collector, result));
    }

    private void requestObjectBodyAndResponse(String method, String path, String contentType, Object body) {
        writeObjectBody(method, path, resolveObjectContentType(contentType), objectBodyWriter -> body);
    }

    private String resolveObjectContentType(String contentType) {
        if (contentType == null)
            contentType = request.contentType();
        if (contentType == null)
            contentType = defaultDocType.replaceFirst("^dal:", "");
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