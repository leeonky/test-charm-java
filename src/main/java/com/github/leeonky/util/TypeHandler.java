package com.github.leeonky.util;

public class TypeHandler<F> {
    private final Class<?> type;

    private final F handler;

    TypeHandler(Class<?> type, F handler) {
        this.type = type;
        this.handler = handler;
    }

    F getHandler() {
        return handler;
    }

    boolean isBaseType(Class<?> type) {
        return this.type.isAssignableFrom(type);
    }

    boolean isPreciseType(Class<?> type) {
        return this.type.equals(type);
    }
}
