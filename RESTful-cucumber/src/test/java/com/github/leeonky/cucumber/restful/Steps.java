package com.github.leeonky.cucumber.restful;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.leeonky.cucumber.restful.extensions.PathVariableReplacement;
import com.github.leeonky.cucumber.restful.spec.FormBeans;
import com.github.leeonky.cucumber.restful.spec.LoginRequests;
import com.github.leeonky.jfactory.JFactory;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.SneakyThrows;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Format;
import org.mockserver.model.Header;
import org.mockserver.model.Parameter;
import org.mockserver.verify.VerificationTimes;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.leeonky.cucumber.restful.RestfulStep.UploadFile.content;
import static com.github.leeonky.dal.Accessors.get;
import static com.github.leeonky.dal.Assertions.expect;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class Steps {
    private static final ClientAndServer mockServer = startClientAndServer(8080);
    private final RestfulStep restfulStep;

    public Steps(RestfulStep restfulStep) {
        this.restfulStep = restfulStep;
    }

    @Before("@jfactory")
    public void setJFactory() {
        JFactory jFactory = new JFactory();
        jFactory.register(LoginRequests.LoginRequest.class).register(FormBeans.DefaultFormBean.class);
        restfulStep.setJFactory(jFactory);
    }

    @Given("base url {string}")
    public void base_url(String baseUrl) {
        restfulStep.setBaseUrl(baseUrl + ":8080");
    }

    @Given("response {int} on {string} {string}:")
    public void responseOnGET(int code, String method, String path, String body) {
        mockServer.when(request().withMethod(method).withPath(path)).respond(response(body).withStatusCode(code));
    }

    @Given("binary response {int} on GET {string}:")
    public void binary_response_on_get(Integer code, String path, String body) {
        mockServer.when(request().withMethod("GET").withPath(path))
                .respond(response().withBody(body.getBytes(StandardCharsets.UTF_8)).withStatusCode(code));
    }

    @Then("{string} got a {string} request on {string}")
    public void got_a_get_request_on(String url, String method, String path) {
        mockServer.verify(request()
                        .withMethod(method)
                        .withPath(path),
                VerificationTimes.once());
    }

    @Then("{string} got a {string} request on {string} with body")
    public void got_a_request_on_with_body(String url, String method, String path, String body) {
        mockServer.verify(request()
                        .withMethod(method)
                        .withPath(path)
                        .withBody(body),
                VerificationTimes.once());
    }

    @SneakyThrows
    @Given("header by RESTful api:")
    public void header_by_res_tful_api(String headerJson) {
        new ObjectMapper().readValue(headerJson, new TypeReference<Map<String, Object>>() {
        }).forEach((key, value) -> {
            if (value instanceof String)
                restfulStep.header(key, (String) value);
            else
                restfulStep.header(key, (List<String>) value);
        });
    }

    @SneakyThrows
    @Then("got request:")
    public void got_request(String expression) {
        verifyRequest(expression);
    }

    public void verifyRequest(String expression) throws JsonProcessingException {
        String content = mockServer.retrieveRecordedRequests(request(), Format.JSON);
        System.out.println("content = " + content);
        expect(new ObjectMapper().readValue(content, List.class)).should(expression);
    }

    @After
    public void stopMockServer() {
        mockServer.reset();
    }

    @Given("var {string} value is {string}")
    public void varValueIs(String varName, String value) {
        PathVariableReplacement.replacements.put(varName, value);
        PathVariableReplacement.evaluator = s -> PathVariableReplacement.replacements.get(s);
    }

    @Given("a file {string}:")
    public void a_file(String fileKey, String fileContent) {
        restfulStep.file(fileKey, content(fileContent));
    }

    @Given("a file {string} with name {string}:")
    public void a_file_with_name(String fileKey, String fileName, String fileContent) {
        restfulStep.file(fileKey, content(fileContent).name(fileName));
    }

    @SneakyThrows
    @Then("got request form data:")
    public void got_request_form_value(String expression) {
        Object request = new ObjectMapper().readValue(mockServer.retrieveRecordedRequests(request(), Format.JSON), List.class);

        byte[] bodyBytes = get("[-1].body.rawBytes.base64").from(request);

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        RequestContext context = new RequestContext() {
            @Override
            public String getCharacterEncoding() {
                return "UTF-8";
            }

            @Override
            public int getContentLength() {
                return bodyBytes.length;
            }

            @Override
            public String getContentType() {
                return get("[-1].headers[Content-Type][0]").from(request);
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(bodyBytes);
            }
        };
        expect(upload.parseRequest(context)).should(expression);
    }

    @Before
    public void noReplacement() {
        PathVariableReplacement.reset();
    }

    @Given("binary response {int} on GET {string} with file name {string}:")
    @SneakyThrows
    public void binaryResponseOnGETWithFileName(int code, String path, String fileName, String body) {
        mockServer.when(request().withMethod("GET").withPath(path))
                .respond(response()
                        .withHeader("Content-Disposition", String.format("attachment; filename=\"%s\"",
                                URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())))
                        .withBody(body.getBytes(StandardCharsets.UTF_8))
                        .withStatusCode(code));
    }

    @SneakyThrows
    @Given("response {int} on {string} {string} with body {string} and headers:")
    public void responseOnWithBodyAndHeaders(int code, String method, String path, String body, String headers) {
        Map<String, Object> headerMap = new ObjectMapper().readValue(headers, Map.class);
        mockServer.when(request().withMethod(method).withPath(path))
                .respond(response()
                        .withBody(body)
                        .withHeaders(headerMap.entrySet().stream().map(entry -> entry.getValue() instanceof List ?
                                        Header.header(entry.getKey(), (List) entry.getValue()) : Header.header(entry.getKey(), (String) entry.getValue()))
                                .collect(Collectors.toList()))
                        .withStatusCode(code));
    }

    @Then("{string} got a {string} request on {string} with params")
    public void gotARequestOnWithParams(String url, String method, String path, DataTable dataTable) {
        mockServer.verify(request()
                        .withMethod(method)
                        .withPath(path)
                        .withQueryStringParameters(dataTable.asMaps().stream()
                                .flatMap(map -> map.entrySet().stream())
                                .map(entry -> Parameter.param(entry.getKey(), entry.getValue())).toArray(Parameter[]::new)),
                VerificationTimes.once());
    }

    @Then("{string} got a {string} request on {string} with params {string} and body")
    public void gotARequestOnWithParamsAndBody(String url, String method, String path, String params, String body) {
        mockServer.verify(request()
                        .withMethod(method)
                        .withPath(path)
                        .withQueryStringParameters(Arrays.stream(params.split("&"))
                                .map(param -> {
                                    String[] nameAndValue = param.split("=");
                                    return Parameter.param(nameAndValue[0], nameAndValue[1]);
                                }).toArray(Parameter[]::new))
                        .withBody(body),
                VerificationTimes.once());
    }

    @SneakyThrows
    @Then("{string} got a {string} request on {string} with body matching")
    public void gotARequestOnWithBodyMatching(String url, String method, String path, String bodyExpression) {
        String receivedRequest = mockServer.retrieveRecordedRequests(request().withMethod(method).withPath(path), Format.JSON);
        expect(new ObjectMapper().readValue(receivedRequest, List.class)).should(bodyExpression);
    }

    @SneakyThrows
    @Given("an external file {string}:")
    public void anExternalFile(String path, String docString) {
        Files.write(Paths.get(path), docString.getBytes());
    }

    @SneakyThrows
    @Then("{string} got a {string} request on {string} with params from docstring")
    public void gotARequestOnWithParamsFromDocstring(String url, String method, String path, String paramsExpression) {
        String receivedRequest = mockServer.retrieveRecordedRequests(request().withMethod(method).withPath(path), Format.JSON);
        expect(new ObjectMapper().readValue(receivedRequest, List.class)).should(paramsExpression);
    }
}
