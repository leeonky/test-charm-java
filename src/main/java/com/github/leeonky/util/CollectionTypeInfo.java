package com.github.leeonky.util;

import java.util.Map;

import static java.util.Collections.emptyMap;

class CollectionTypeInfo<T> implements TypeInfo<T> {
    private final BeanClass<T> type;

    public CollectionTypeInfo(BeanClass<T> type) {
        this.type = type;
    }

    @Override
    public PropertyReader<T> getReader(String property) {
        return new CollectionDataPropertyReader<>(type, property, type.getElementType());
    }

    @Override
    public PropertyWriter<T> getWriter(String property) {
        return new CollectionDataPropertyWriter<>(type, property, type.getElementType());
    }

    @Override
    public Map<String, PropertyReader<T>> getReaders() {
        return emptyMap();
    }

    @Override
    public Map<String, PropertyWriter<T>> getWriters() {
        return emptyMap();
    }

    @Override
    public Map<String, Property<T>> getProperties() {
        return emptyMap();
    }

    @Override
    public Property<T> getProperty(String name) {
        //TODO not implement
        return null;
    }
}
