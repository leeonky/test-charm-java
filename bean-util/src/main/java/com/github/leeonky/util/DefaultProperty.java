package com.github.leeonky.util;

import static java.lang.String.format;

class DefaultProperty<T> implements Property<T> {
    private final String name;
    private final BeanClass<T> beanType;

    public DefaultProperty(String name, BeanClass<T> beanType) {
        this.name = name;
        this.beanType = beanType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BeanClass<T> getBeanType() {
        return beanType;
    }

    @Override
    public PropertyReader<T> getReader() {
        return beanType.getPropertyReader(name);
    }

    @Override
    public PropertyWriter<T> getWriter() {
        return beanType.getPropertyWriter(name);
    }

    @Override
    public String toString() {
        return format("%s.%s", beanType.getName(), name);
    }
}
