package com.github.leeonky.util;

public interface PropertyReader<T> {
    Object getValue(T bean);

    String getName();
}
