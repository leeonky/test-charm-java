package com.github.leeonky.util;

public interface PropertyWriter<T> extends Property {
    void setValue(T bean, Object value);
}
