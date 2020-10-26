package com.github.leeonky.util;

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
}
