package com.github.leeonky.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class BeanClass<T> {
    private final Map<String, PropertyReader<T>> readers = new LinkedHashMap<>();
    private final Class<T> type;

    public BeanClass(Class<T> type) {
        this.type = type;
        for (Field field : type.getFields())
            if (FieldPropertyReader.isCandidate(field))
                addReader(new FieldPropertyReader<>(field));
        for (Method method : type.getMethods())
            if (MethodPropertyReader.isGetter(method))
                addReader(new MethodPropertyReader<>(method));
    }

    private void addReader(PropertyReader<T> reader) {
        readers.put(reader.getName(), reader);
    }

    public Object getPropertyValue(String field, T bean) {
        return getPropertyReader(field).getValue(bean);
    }

    private PropertyReader<T> getPropertyReader(String field) {
        PropertyReader<T> reader = readers.get(field);
        if (reader == null)
            throw new IllegalArgumentException("No available property reader for " + type.getSimpleName() + "." + field);
        return reader;
    }
}
