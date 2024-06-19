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
}
