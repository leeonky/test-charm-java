package com.github.leeonky.util;

import java.util.Map;

import static java.util.Collections.emptyMap;

class CollectionTypeInfo<T> extends ClassTypeInfo<T> {

    public CollectionTypeInfo(BeanClass<T> type) {
        super(type);
    }

    @Override
    public PropertyReader<T> getReader(String property) {
        try {
            Integer.valueOf(property);
            return new CollectionDataPropertyReader<>(type, property, type.getElementType());
        } catch (NumberFormatException ignore) {
            return super.getReader(property);
        }
    }

    @Override
    public PropertyWriter<T> getWriter(String property) {
        try {
            Integer.valueOf(property);
            return new CollectionDataPropertyWriter<>(type, property, type.getElementType());
        } catch (NumberFormatException ignore) {
            return super.getWriter(property);
        }
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
        return new DefaultProperty<>(name, type);
    }
}
