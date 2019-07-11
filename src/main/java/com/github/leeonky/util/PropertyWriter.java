package com.github.leeonky.util;

public interface PropertyWriter<T> extends Property<T> {
    void setValue(T bean, Object value);
}
