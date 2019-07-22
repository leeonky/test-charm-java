package com.github.leeonky.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeanClass<T> {
    private final Map<String, PropertyReader<T>> readers = new LinkedHashMap<>();
    private final Map<String, PropertyWriter<T>> writers = new LinkedHashMap<>();
    private final Class<T> type;
    private final Converter converter;

    public BeanClass(Class<T> type) {
        this(type, Converter.createDefaultConverter());
    }

    public BeanClass(Class<T> type, Converter converter) {
        this.type = type;
        this.converter = converter;
        for (Field field : type.getFields()) {
            addReader(new FieldPropertyReader<>(this, field));
            addWriter(new FieldPropertyWriter<>(this, field));
        }
        for (Method method : type.getMethods()) {
            if (MethodPropertyReader.isGetter(method))
                addReader(new MethodPropertyReader<>(this, method));
            if (MethodPropertyWriter.isSetter(method))
                addWriter(new MethodPropertyWriter<>(this, method));
        }
    }

    public static String getClassName(Object object) {
        return object == null ? null : object.getClass().getName();
    }

    public Converter getConverter() {
        return converter;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return type.getName();
    }

    public String getSimpleName() {
        return type.getSimpleName();
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

    public Object getPropertyValue(T bean, String field) {
        return getPropertyReader(field).getValue(bean);
    }

    public PropertyReader<T> getPropertyReader(String field) {
        PropertyReader<T> reader = readers.get(field);
        if (reader == null)
            throw new IllegalArgumentException("No available property reader for " + type.getSimpleName() + "." + field);
        return reader;
    }

    public BeanClass<T> setPropertyValue(T bean, String field, Object value) {
        getPropertyWriter(field).setValue(bean, value);
        return this;
    }

    public PropertyWriter<T> getPropertyWriter(String field) {
        PropertyWriter<T> writer = writers.get(field);
        if (writer == null)
            throw new IllegalArgumentException("No available property writer for " + type.getSimpleName() + "." + field);
        return writer;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(Object... args) {
        try {
            List<Constructor<?>> constructors = Stream.of(type.getConstructors())
                    .filter(c -> isRightConstructor(c, args))
                    .collect(Collectors.toList());
            if (constructors.size() != 1)
                throw new IllegalArgumentException(String.format("No appropriate %s constructor for params [%s]",
                        type.getName(), toString(args)));
            return (T) constructors.get(0).newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private String toString(Object[] args) {
        return Stream.of(args)
                .map(o -> o == null ? "null" : o.getClass().getName() + ":" + o)
                .collect(Collectors.joining(", "));
    }

    private boolean isRightConstructor(Constructor<?> constructor, Object[] args) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (parameterTypes.length == args.length) {
            for (int i = 0; i < parameterTypes.length; i++)
                if (!parameterTypes[i].isInstance(args[i]))
                    return false;
            return true;
        }
        return false;
    }
}
