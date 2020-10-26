package com.github.leeonky.util;

public interface PropertyWriter<T> extends PropertyAccessor<T> {
    void setValue(T bean, Object value);
}
