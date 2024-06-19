package com.github.leeonky.util;

import java.util.Map;

interface TypeInfo<T> {
    static <T> TypeInfo<T> create(BeanClass<T> type) {
        if (type.isCollection())
            return new CollectionTypeInfo<>(type);
        return new ClassTypeInfo<>(type);
    }

    PropertyReader<T> getReader(String property);

    PropertyWriter<T> getWriter(String property);

    Map<String, PropertyReader<T>> getReaders();

    Map<String, PropertyWriter<T>> getWriters();

    Map<String, Property<T>> getProperties();

    Property<T> getProperty(String name);
}
