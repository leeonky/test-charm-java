package com.github.leeonky.util;

public interface PropertyWriter<T> {
    void setValue(T bean, Object value);

    String getName();
}
