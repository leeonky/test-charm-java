package com.github.leeonky.util;

import java.lang.annotation.Annotation;

public interface Property<T> {

    String getName();

    Class<?> getPropertyClass();

    Object tryConvert(Object value);

    BeanClass<T> getBeanClass();

    BeanClass<?> getPropertyType();

    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

}
