package com.github.leeonky.util;

import java.lang.annotation.Annotation;

public interface Property {

    String getName();

    Class<?> getPropertyType();

    default BeanClass<?> getPropertyTypeWrapper() {
        return new BeanClass<>(getPropertyType());
    }

    <A extends Annotation> A getAnnotation(Class<A> annotationClass);
}
