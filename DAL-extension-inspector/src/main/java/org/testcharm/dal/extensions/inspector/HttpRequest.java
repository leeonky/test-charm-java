package org.testcharm.dal.extensions.inspector;

import org.testcharm.util.Sneaky;

import javax.annotation.Generated;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.Integer.parseInt;

@Generated("AI generated")
class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final byte[] body;

    private HttpRequest(String method, String path, Map<String, String> headers, Map<String, String> queryParams, byte[] body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.queryParams = queryParams;
        this.body = body;
    }

    String method() {
        return method;
    }

    String path() {
        return path;
    }

    String header(String name) {
        return headers.get(name.toLowerCase(Locale.ROOT));
    }

    String query(String name) {
        return queryParams.getOrDefault(name, "");
    }

    String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }

    boolean isWebSocketUpgrade() {
        String upgrade = header("upgrade");
        String connection = header("connection");
        return "websocket".equalsIgnoreCase(upgrade)
                && connection != null
                && connection.toLowerCase(Locale.ROOT).contains("upgrade");
    }

    static HttpRequest read(InputStream in) throws IOException {
        byte[] headerBytes = readHeaders(in);
        if (headerBytes == null || headerBytes.length == 0) {
            return null;
        }
        String head = new String(headerBytes, StandardCharsets.ISO_8859_1);
        String[] lines = head.split("\\r\\n");
        if (lines.length == 0) {
            return null;
        }

        String[] requestLine = lines[0].split(" ");
        if (requestLine.length < 2) {
            return null;
        }
        String method = requestLine[0].trim();
        String target = requestLine[1].trim();

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                continue;
            }
            int idx = line.indexOf(':');
            if (idx <= 0) {
                continue;
            }
            headers.put(line.substring(0, idx).trim().toLowerCase(Locale.ROOT), line.substring(idx + 1).trim());
        }

        int contentLength = 0;
        if (headers.containsKey("content-length")) {
            try {
                contentLength = parseInt(headers.get("content-length"));
            } catch (NumberFormatException ignore) {
                contentLength = 0;
            }
        }
        byte[] body = new byte[Math.max(contentLength, 0)];
        if (contentLength > 0) {
            int offset = 0;
            while (offset < contentLength) {
                int read = in.read(body, offset, contentLength - offset);
                if (read == -1) {
                    throw new EOFException();
                }
                offset += read;
            }
        }

        int queryIndex = target.indexOf('?');
        String path = queryIndex < 0 ? target : target.substring(0, queryIndex);
        Map<String, String> query = queryIndex < 0 ? Collections.emptyMap() : parseQuery(target.substring(queryIndex + 1));

        return new HttpRequest(method, path, headers, query, body);
    }

    private static byte[] readHeaders(InputStream in) throws IOException {
        ByteArrayOutputStream headers = new ByteArrayOutputStream();
        int matched = 0;
        while (true) {
            int b = in.read();
            if (b == -1) {
                return headers.size() == 0 ? null : headers.toByteArray();
            }
            headers.write(b);
            if ((matched == 0 && b == '\r')
                    || (matched == 1 && b == '\n')
                    || (matched == 2 && b == '\r')
                    || (matched == 3 && b == '\n')) {
                matched++;
                if (matched == 4) {
                    byte[] all = headers.toByteArray();
                    return Arrays.copyOf(all, all.length - 4);
                }
            } else {
                matched = b == '\r' ? 1 : 0;
            }

            if (headers.size() > 65536) {
                throw new IOException("HTTP headers too large");
            }
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> values = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return values;
        }
        for (String item : query.split("&")) {
            int idx = item.indexOf('=');
            String key = idx < 0 ? item : item.substring(0, idx);
            String value = idx < 0 ? "" : item.substring(idx + 1);
            values.put(urlDecode(key), urlDecode(value));
        }
        return values;
    }

    private static String urlDecode(String value) {
        return Sneaky.get(() -> URLDecoder.decode(value, StandardCharsets.UTF_8.name()));
    }
}
