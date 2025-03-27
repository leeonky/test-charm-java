package com.github.leeonky.dal.extensions.inspector.cucumber.ui;

public interface Target<P> {
    void navigateTo();

    P create();

    default boolean matches(P current) {
        return false;
    }
}
