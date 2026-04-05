package org.testcharm.cucumber.restful;

public interface BodyRequestBuilder<T> {

    boolean matches(String contentType);

    T writer(String contentType);
}
