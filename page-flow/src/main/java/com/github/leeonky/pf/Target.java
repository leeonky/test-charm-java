package com.github.leeonky.pf;

public interface Target<P> {
    void navigateTo();

    P create();

    default boolean matches(P current) {
        return false;
    }
}
