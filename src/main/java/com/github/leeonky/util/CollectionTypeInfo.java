package com.github.leeonky.util;

import java.util.Collections;
import java.util.Map;

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
        return null;
    }

    @Override
    public Map<String, PropertyReader<T>> getReaders() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, PropertyWriter<T>> getWriters() {
        return null;
    }
}
