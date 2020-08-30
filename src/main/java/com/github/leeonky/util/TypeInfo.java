package com.github.leeonky.util;

import java.util.Map;

interface TypeInfo<T> {
    static <T> TypeInfo<T> create(BeanClass<T> type) {
        return new TypeTypeInfo<>(type);
    }

    Map<String, PropertyReader<T>> getReaders();

    Map<String, PropertyWriter<T>> getWriters();
}
