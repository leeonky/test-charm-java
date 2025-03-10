package com.github.leeonky.cucumber.restful;

import com.github.leeonky.util.Sneaky;
import com.github.leeonky.util.ThrowingRunnable;

import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.charset.Charset;

class HttpStream {
    private final OutputStream outputStream;
    private final Charset charset;

    public HttpStream(OutputStream outputStream, Charset charset) {
        this.outputStream = outputStream;
        this.charset = charset;
    }

    public HttpStream appendFile(String key, RestfulStep.UploadFile uploadFile) {
        append(String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"",
                key.substring(1), uploadFile.getName())).crlf()
                .append("Content-Type: " + URLConnection.guessContentTypeFromName(uploadFile.getName())).crlf()
                .append("Content-Transfer-Encoding: binary").crlf().crlf()
                .append(uploadFile.getContent());
        return this;
    }

    public HttpStream appendField(String key, String value) {
        append("Content-Disposition: form-data; name=\"" + key + "\"").crlf()
                .append("Content-Type: text/plain; charset=" + RestfulStep.CHARSET).crlf().crlf()
                .append(value);
        return this;
    }

    public void close(String boundary) {
        Sneaky.run(append("--" + boundary + "--").crlf().outputStream::close);
    }

    public void bound(String boundary, ThrowingRunnable bound) {
        append("--" + boundary).crlf();
        Sneaky.run(bound);
        crlf();
    }

    private HttpStream append(String content) {
        Sneaky.run(() -> outputStream.write(content.getBytes(charset)));
        return this;
    }

    private HttpStream append(byte[] content) {
        Sneaky.run(() -> outputStream.write(content));
        return this;
    }

    private HttpStream crlf() {
        Sneaky.run(() -> outputStream.write("\r\n".getBytes(charset)));
        return this;
    }
}
