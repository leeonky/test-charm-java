package com.github.leeonky.util;

import java.lang.annotation.Annotation;

public interface Property<T> {

    String getName();

    Class<?> getPropertyType();

    Object tryConvert(Object value);

    BeanClass<T> getBeanClass();

    BeanClass<?> getPropertyTypeWrapper();

    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    @Deprecated
    default Class<?> getElementType() {
        Class<?> propertyType = getPropertyType();
        if (propertyType.isArray())
            return propertyType.getComponentType();
        if (Iterable.class.isAssignableFrom(propertyType))
            return getPropertyTypeWrapper().getTypeArguments(0)
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Should specify generic type %s.%s", getBeanClass().getName(), getName()))).getType();
        return null;
    }

    @Deprecated
    default Class<?> getElementOrPropertyType() {
        Class<?> elementType = getElementType();
        return elementType == null ? getPropertyType() : elementType;
    }
}
