package com.github.leeonky.util;

public interface PropertyReader<T> extends Property {
    Object getValue(T bean);
}
