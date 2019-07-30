package com.github.leeonky.util;

import java.lang.annotation.Annotation;

public interface Property<T> {

    String getName();

    Class<?> getPropertyType();

    Object tryConvert(Object value);

    BeanClass<T> getBeanClass();

    default BeanClass<?> getPropertyTypeWrapper() {
        return BeanClass.create(getPropertyType());
    }

    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    GenericType getGenericType();
}
