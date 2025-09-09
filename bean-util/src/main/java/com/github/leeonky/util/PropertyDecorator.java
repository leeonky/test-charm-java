package com.github.leeonky.util;

public class PropertyDecorator<T> implements Property<T> {
    private final Property<T> property;

    public PropertyDecorator(Property<T> property) {
        this.property = property;
    }

    @Override
    public String getName() {
        return property.getName();
    }

    @Override
    public BeanClass<T> getBeanType() {
        return property.getBeanType();
    }

    @Override
    public PropertyReader<T> getReader() {
        return property.getReader();
    }

    @Override
    public PropertyWriter<T> getWriter() {
        return property.getWriter();
    }
}
