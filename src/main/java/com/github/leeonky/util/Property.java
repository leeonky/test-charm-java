package com.github.leeonky.util;

public interface Property<T> {
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
