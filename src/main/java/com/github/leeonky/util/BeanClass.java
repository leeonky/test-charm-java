package com.github.leeonky.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class BeanClass<T> {
    private final Map<String, PropertyReader<T>> readers = new LinkedHashMap<>();
    private final Map<String, PropertyWriter<T>> writers = new LinkedHashMap<>();
    private final Class<T> type;

    public BeanClass(Class<T> type) {
        this.type = type;
        for (Field field : type.getFields()) {
            addReader(new FieldPropertyReader<>(field));
            addWriter(new FieldPropertyWriter<>(field));
        }
        for (Method method : type.getMethods()) {
            if (MethodPropertyReader.isGetter(method))
                addReader(new MethodPropertyReader<>(method));
            if (MethodPropertyWriter.isSetter(method))
                addWriter(new MethodPropertyWriter<>(method));
        }
    }

    public Map<String, PropertyReader<T>> getPropertyReaders() {
        return readers;
    }

    public Map<String, PropertyWriter<T>> getPropertyWriters() {
        return writers;
    }

    private void addReader(PropertyReader<T> reader) {
        readers.put(reader.getName(), reader);
    }

    private void addWriter(PropertyWriter<T> writer) {
        writers.put(writer.getName(), writer);
    }

    public Object getPropertyValue(String field, T bean) {
        return getPropertyReader(field).getValue(bean);
    }

    public PropertyReader<T> getPropertyReader(String field) {
        PropertyReader<T> reader = readers.get(field);
        if (reader == null)
            throw new IllegalArgumentException("No available property reader for " + type.getSimpleName() + "." + field);
        return reader;
    }

    public void setPropertyValue(String field, T bean, Object value) {
        getPropertyWriter(field).setValue(bean, value);
    }

    public PropertyWriter<T> getPropertyWriter(String field) {
        PropertyWriter<T> writer = writers.get(field);
        if (writer == null)
            throw new IllegalArgumentException("No available property writer for " + type.getSimpleName() + "." + field);
        return writer;
    }
}
