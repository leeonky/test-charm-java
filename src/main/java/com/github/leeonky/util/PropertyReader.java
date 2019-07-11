package com.github.leeonky.util;

public interface PropertyReader<T> extends Property<T> {
    Object getValue(T bean);
}
