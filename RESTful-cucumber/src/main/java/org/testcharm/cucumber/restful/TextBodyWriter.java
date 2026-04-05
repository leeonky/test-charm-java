package org.testcharm.cucumber.restful;

import java.io.IOException;
import java.io.OutputStream;

public interface TextBodyWriter {
    default String contentType(String contentType) {
        return contentType;
    }

    void write(OutputStream outputStream, String content) throws IOException;
}
