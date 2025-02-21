package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public interface Target<P> {
    void navigateTo();

    P create();

    default boolean matches(P current) {
        return false;
    }
}
