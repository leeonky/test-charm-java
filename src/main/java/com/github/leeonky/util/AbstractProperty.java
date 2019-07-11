package com.github.leeonky.util;

abstract class AbstractProperty<T> implements Property<T> {
    private final BeanClass beanClass;

    AbstractProperty(BeanClass<T> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public Object tryConvert(Object value) {
        return beanClass.getConverter().tryConvert(getPropertyType(), value);
    }

    @Override
    public BeanClass<T> getBeanClass() {
        return beanClass;
    }
}
