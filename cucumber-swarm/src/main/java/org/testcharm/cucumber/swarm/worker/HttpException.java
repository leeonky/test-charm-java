package org.testcharm.cucumber.swarm.worker;

import java.net.HttpURLConnection;

public class HttpException extends RuntimeException {
    private final HttpURLConnection urlConnection;

    public HttpException(HttpURLConnection urlConnection) {
        this.urlConnection = urlConnection;
    }
}
