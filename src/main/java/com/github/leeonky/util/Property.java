package com.github.leeonky.util;

public interface Property {

    String getName();

    Class<?> getPropertyType();

    default BeanClass<?> getPropertyTypeWrapper() {
        return new BeanClass<>(getPropertyType());
    }
}
