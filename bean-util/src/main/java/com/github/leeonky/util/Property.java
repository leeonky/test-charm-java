package com.github.leeonky.util;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public interface Property<T> {
    static List<Object> toChainNodes(String chain) {
        return Arrays.stream(chain.split("[\\[\\].]")).filter(s -> !s.isEmpty()).map(s -> {
            try {
                return Integer.valueOf(s);
            } catch (Exception ignore) {
                return s;
            }
        }).collect(toList());
    }

    String getName();

    BeanClass<T> getBeanType();

    PropertyReader<T> getReader();

    PropertyWriter<T> getWriter();

    @SuppressWarnings("unchecked")
    default <P> BeanClass<P> getReaderType() {
        return (BeanClass<P>) getReader().getType();
    }

    @SuppressWarnings("unchecked")
    default <P> BeanClass<P> getWriterType() {
        return (BeanClass<P>) getWriter().getType();
    }

    default Property<T> setValue(T instance, Object value) {
        getWriter().setValue(instance, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    default <P> P getValue(T instance) {
        return (P) getReader().getValue(instance);
    }

    default Property<T> decorateReaderType(BeanClass<?> newType) {
        PropertyReader<T> reader = getReader();
        if (reader.getType().equals(newType))
            return this;
        Class<?> type = reader.getOriginType().getType();
        if (newType.isInheritedFrom(type))
            return new PropertyDecorator<T>(this) {
                @Override
                public PropertyReader<T> getReader() {
                    return reader.decorateType(newType);
                }
            };
        throw new IllegalStateException("Type " + newType.getType() + " is not inherited from " + type);
    }

    default Property<T> decorateWriterType(BeanClass<?> newType) {
        PropertyWriter<T> writer = getWriter();
        if (writer.getType().equals(newType))
            return this;
        Class<?> type = writer.getOriginType().getType();
        if (newType.isInheritedFrom(type))
            return new PropertyDecorator<T>(this) {
                @Override
                public PropertyWriter<T> getWriter() {
                    return writer.decorateType(newType);
                }
            };
        throw new IllegalStateException("Type " + newType.getType() + " is not inherited from " + type);
    }

    default Property<T> decorateType(BeanClass<?> newType) {
        return decorateWriterType(newType).decorateReaderType(newType);
    }
}
