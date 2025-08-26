package com.github.leeonky.util;

abstract class AbstractPropertyAccessor<T> implements PropertyAccessor<T> {
    private final BeanClass<T> beanClass;

    AbstractPropertyAccessor(BeanClass<T> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public Object tryConvert(Object value) {
        return BeanClass.getConverter().tryConvert(getType().getType(), value);
    }

    @Override
    public BeanClass<T> getBeanType() {
        return beanClass;
    }

    @Override
    public BeanClass<?> getType() {
        return BeanClass.create(GenericType.createGenericType(getGenericType()));
    }
}
