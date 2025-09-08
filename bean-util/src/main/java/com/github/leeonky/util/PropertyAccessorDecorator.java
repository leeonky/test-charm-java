package com.github.leeonky.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public abstract class PropertyAccessorDecorator<T> implements PropertyAccessor<T> {
    protected final PropertyReader<T> reader;

    public PropertyAccessorDecorator(PropertyReader<T> reader) {
        this.reader = reader;
    }

    @Override
    public String getName() {
        return reader.getName();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return reader.getAnnotation(annotationClass);
    }

    @Override
    public boolean isBeanProperty() {
        return reader.isBeanProperty();
    }

    @Override
    public BeanClass<T> getBeanType() {
        return reader.getBeanType();
    }

    @Override
    public Object tryConvert(Object value) {
        return BeanClass.getConverter().tryConvert(getType().getType(), value);
    }

    @Override
    public Type getGenericType() {
        return reader.getGenericType();
    }

    @Override
    public BeanClass<?> getType() {
        return BeanClass.create(GenericType.createGenericType(getGenericType()));
    }
}
