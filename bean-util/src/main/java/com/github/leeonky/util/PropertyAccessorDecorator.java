package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public abstract class PropertyAccessorDecorator<T, A extends PropertyAccessor<T>> implements PropertyAccessor<T> {
    protected final A accessor;

    public PropertyAccessorDecorator(A accessor) {
        this.accessor = accessor;
    }

    @Override
    public String getName() {
        return accessor.getName();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return accessor.getAnnotation(annotationClass);
    }

    @Override
    public boolean isBeanProperty() {
        return accessor.isBeanProperty();
    }

    @Override
    public BeanClass<T> getBeanType() {
        return accessor.getBeanType();
    }

    @Override
    public Object tryConvert(Object value) {
        return BeanClass.getConverter().tryConvert(getType().getType(), value);
    }

    @Override
    public Type getGenericType() {
        return accessor.getGenericType();
    }

    @Override
    public BeanClass<?> getType() {
        return BeanClass.create(GenericType.createGenericType(getGenericType()));
    }
}
