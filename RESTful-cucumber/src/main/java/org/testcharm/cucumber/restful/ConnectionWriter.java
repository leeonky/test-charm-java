package org.testcharm.cucumber.restful;

import java.net.HttpURLConnection;

public interface ConnectionWriter {
    boolean matches(String contentType);

    void write(String contentType, String[] traitSpec, String bodyContent, HttpURLConnection connection);
}
