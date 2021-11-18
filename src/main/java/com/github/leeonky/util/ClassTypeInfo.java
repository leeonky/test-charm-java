package com.github.leeonky.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

class ClassTypeInfo<T> implements TypeInfo<T> {
    protected final BeanClass<T> type;
    private final Map<String, PropertyReader<T>> readers = new LinkedHashMap<>();
    private final Map<String, PropertyWriter<T>> writers = new LinkedHashMap<>();
    private final Map<String, Property<T>> properties = new LinkedHashMap<>();

    public ClassTypeInfo(BeanClass<T> type) {
        this.type = type;
        Map<String, Field> addedReaderFields = new HashMap<>();
        Map<String, Field> addedWriterFields = new HashMap<>();

        for (Field field : type.getType().getFields()) {
            Field addedReaderField = addedReaderFields.get(field.getName());
            if (addedReaderField == null || addedReaderField.getType().equals(type.getType())) {
                addAccessor(new FieldPropertyReader<>(type, field), readers);
                addedReaderFields.put(field.getName(), field);
            }
            Field addedWriterField = addedWriterFields.get(field.getName());
            if (addedWriterField == null || addedWriterField.getType().equals(type.getType())) {
                addAccessor(new FieldPropertyWriter<>(type, field), writers);
                addedWriterFields.put(field.getName(), field);
            }
        }
        for (Method method : type.getType().getMethods()) {
            if (MethodPropertyReader.isGetter(method))
                addAccessor(new MethodPropertyReader<>(type, method), readers);
            if (MethodPropertyWriter.isSetter(method))
                addAccessor(new MethodPropertyWriter<>(type, method), writers);
        }
    }

    private <A extends PropertyAccessor<T>> void addAccessor(A accessor, Map<String, A> accessorMap) {
        properties.put(accessor.getName(), new DefaultProperty<>(accessor.getName(), accessor.getBeanType()));
        accessorMap.put(accessor.getName(), accessor);
    }

    @Override
    public PropertyReader<T> getReader(String property) {
        return readers.computeIfAbsent(property, k -> {
            throw new NoSuchAccessorException("No available property reader for " + type.getSimpleName() + "." + property);
        });
    }

    @Override
    public PropertyWriter<T> getWriter(String property) {
        return writers.computeIfAbsent(property, k -> {
            throw new NoSuchAccessorException("No available property writer for " + type.getSimpleName() + "." + property);
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

    @Override
    public Map<String, Property<T>> getProperties() {
        return properties;
    }

    @Override
    public Property<T> getProperty(String name) {
        return properties.computeIfAbsent(name, k -> {
            throw new NoSuchPropertyException(type.getSimpleName() + "." + name);
        });
    }
}
