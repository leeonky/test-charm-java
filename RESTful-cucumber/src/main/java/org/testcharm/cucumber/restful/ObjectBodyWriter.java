package org.testcharm.cucumber.restful;

import org.testcharm.util.Collector;

import java.io.IOException;
import java.io.OutputStream;

public interface ObjectBodyWriter {
    default String contentType(String contentType) {
        return contentType;
    }

    void write(OutputStream outputStream, Collector collector, Object result) throws IOException;
}
