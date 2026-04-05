package org.testcharm.cucumber.restful;

import org.testcharm.util.Collector;

import java.io.IOException;
import java.io.OutputStream;

public interface ObjectBodyWriter {
    default String contentType(String contentType) {
        return contentType;
    }

    default Object body(Collector collector, Object result) {
        return collector.build();
    }

    void write(OutputStream outputStream, Object body) throws IOException;
}
