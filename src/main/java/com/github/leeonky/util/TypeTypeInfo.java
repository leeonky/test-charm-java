package com.github.leeonky.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

class TypeTypeInfo<T> implements TypeInfo<T> {
    private final Map<String, PropertyReader<T>> readers = new LinkedHashMap<>();
    private final Map<String, PropertyWriter<T>> writers = new LinkedHashMap<>();
    private final BeanClass<T> type;

    public TypeTypeInfo(BeanClass<T> type) {
        this.type = type;
        Map<String, Field> addedReaderFields = new HashMap<>();
        Map<String, Field> addedWriterFields = new HashMap<>();

        for (Field field : type.getType().getFields()) {
            Field addedReaderField = addedReaderFields.get(field.getName());
            if (addedReaderField == null || addedReaderField.getType().equals(type.getType())) {
                addReader(new FieldPropertyReader<>(type, field));
                addedReaderFields.put(field.getName(), field);
            }
            Field addedWriterField = addedWriterFields.get(field.getName());
            if (addedWriterField == null || addedWriterField.getType().equals(type.getType())) {
                addWriter(new FieldPropertyWriter<>(type, field));
                addedWriterFields.put(field.getName(), field);
            }
        }
        for (Method method : type.getType().getMethods()) {
            if (MethodPropertyReader.isGetter(method))
                addReader(new MethodPropertyReader<>(type, method));
            if (MethodPropertyWriter.isSetter(method))
                addWriter(new MethodPropertyWriter<>(type, method));
        }
    }

    private void addReader(PropertyReader<T> reader) {
        readers.put(reader.getName(), reader);
    }

    private void addWriter(PropertyWriter<T> writer) {
        writers.put(writer.getName(), writer);
    }

    @Override
    public PropertyReader<T> getReader(String property) {
        return readers.computeIfAbsent(property, k -> {
            throw new IllegalArgumentException("No available property reader for " + type.getSimpleName() + "." + property);
        });
    }

    @Override
    public PropertyWriter<T> getWriter(String property) {
        return writers.computeIfAbsent(property, k -> {
            throw new IllegalArgumentException("No available property writer for " + type.getSimpleName() + "." + property);
        });
    }

    @Override
    public Map<String, PropertyReader<T>> getReaders() {
        return readers;
    }

    @Override
    public Map<String, PropertyWriter<T>> getWriters() {
        return writers;
    }
}
