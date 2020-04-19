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

    default Class<?> getElementType() {
        Class<?> propertyType = getPropertyType();
        if (propertyType.isArray())
            return propertyType.getComponentType();
        if (Iterable.class.isAssignableFrom(propertyType))
            return getGenericType().getGenericTypeParameter(0)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Should specify generic type %s.%s", getBeanClass().getName(), getName()))).getRawType();
        return null;
    }

    default Class<?> getElementOrPropertyType() {
        Class<?> elementType = getElementType();
        return elementType == null ? getPropertyType() : elementType;
    }
}
