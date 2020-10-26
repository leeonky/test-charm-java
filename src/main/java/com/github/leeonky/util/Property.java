package com.github.leeonky.util;

public interface Property<T> {
    String getName();

    BeanClass<T> getBeanType();
}
