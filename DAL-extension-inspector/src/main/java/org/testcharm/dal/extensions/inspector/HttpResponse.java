package org.testcharm.dal.extensions.inspector;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class HttpResponse {
    private final int code;
    private final String status;
    private final byte[] body;
    private final Map<String, String> headers;

    private HttpResponse(int code, String status, byte[] body, Map<String, String> headers) {
        this.code = code;
        this.status = status;
        this.body = body;
        this.headers = headers;
    }

    static HttpResponse ok() {
        return text("");
    }

    static HttpResponse text(String value) {
        return withBody(200, "OK", value.getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");
    }

    static HttpResponse xml(String value) {
        return withBody(200, "OK", value.getBytes(StandardCharsets.UTF_8), "application/xml; charset=utf-8");
    }

    static HttpResponse binary(byte[] body, String contentType) {
        return withBody(200, "OK", body, contentType);
    }

    static HttpResponse redirect(String location) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", location);
        headers.put("Content-Length", "0");
        return new HttpResponse(302, "Found", new byte[0], headers);
    }

    static HttpResponse notFound() {
        return withBody(404, "Not Found", "Not Found".getBytes(StandardCharsets.UTF_8), "text/plain; charset=utf-8");
    }

    private static HttpResponse withBody(int code, String status, byte[] body, String contentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        headers.put("Content-Length", String.valueOf(body.length));
        return new HttpResponse(code, status, body, headers);
    }

    void write(OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(code).append(' ').append(status).append("\r\n");
        sb.append("Connection: close\r\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.write(body);
        out.flush();
    }
}
