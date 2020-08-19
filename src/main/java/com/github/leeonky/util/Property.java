package com.github.leeonky.util;

import java.lang.annotation.Annotation;

public interface Property<T> {

    String getName();

    Object tryConvert(Object value);

    BeanClass<T> getBeanType();

    BeanClass<?> getType();

    default Class<?> getTypeClass() {
        return getType().getType();
    }

    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

}
