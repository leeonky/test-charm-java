package com.github.leeonky.util;

import java.lang.reflect.Type;

abstract class AbstractPropertyAccessor<T> implements PropertyAccessor<T> {
    private final BeanClass<T> beanClass;

    AbstractPropertyAccessor(BeanClass<T> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public Object tryConvert(Object value) {
        return BeanClass.getConverter().tryConvert(getTypeClass(), value);
    }

    @Override
    public BeanClass<T> getBeanType() {
        return beanClass;
    }

    protected abstract Type provideGenericType();

    @Override
    public BeanClass<?> getType() {
        return BeanClass.create(GenericType.createGenericType(provideGenericType()));
    }
}
