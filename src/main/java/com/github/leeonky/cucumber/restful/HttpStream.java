package com.github.leeonky.cucumber.restful;

import com.github.leeonky.util.Suppressor;
import com.github.leeonky.util.ThrowingRunnable;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;

class HttpStream {
    private final OutputStream outputStream;
    private final Charset charset;

    public HttpStream(OutputStream outputStream, Charset charset) {
        this.outputStream = outputStream;
        this.charset = charset;
    }

    private HttpStream append(String content) {
        Suppressor.run(() -> outputStream.write(content.getBytes(charset)));
        return this;
    }

    private HttpStream append(byte[] content) {
        Suppressor.run(() -> outputStream.write(content));
        return this;
    }

    private HttpStream crlf() {
        Suppressor.run(() -> outputStream.write("\r\n".getBytes(charset)));
        return this;
    }

    public HttpStream appendFile(String key, RestfulStep.UploadFile uploadFile) throws UnsupportedEncodingException {
        append(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"",
                URLEncoder.encode(key.substring(1), RestfulStep.CHARSET), URLEncoder.encode(uploadFile.getName(), RestfulStep.CHARSET))).crlf()
                .append("Content-Type: " + URLConnection.guessContentTypeFromName(uploadFile.getName())).crlf()
                .append("Content-Transfer-Encoding: binary").crlf().crlf()
                .append(uploadFile.getContent());
        return this;
    }

    public HttpStream appendField(String key, String value) throws UnsupportedEncodingException {
        append("Content-Disposition: form-data; name=\"" + URLEncoder.encode(key, RestfulStep.CHARSET) + "\"").crlf()
                .append("Content-Type: text/plain; charset=" + RestfulStep.CHARSET).crlf().crlf()
                .append(URLEncoder.encode(value, RestfulStep.CHARSET));
        return this;
    }

    public void close(String boundary) {
        Suppressor.run(append("--" + boundary + "--").crlf().outputStream::close);
    }

    public void bound(String boundary, ThrowingRunnable bound) {
        append("--" + boundary).crlf();
        Suppressor.run(bound);
        crlf();
    }
}
