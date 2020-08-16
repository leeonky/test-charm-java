package com.github.leeonky.util;

import java.util.Optional;

public class GenericBeanClass<T> extends BeanClass<T> {
    private final GenericType genericType;

    @SuppressWarnings("unchecked")
    private GenericBeanClass(GenericType genericType) {
        super((Class<T>) genericType.getRawType());
        this.genericType = genericType;
    }

    public static BeanClass<?> create(GenericType genericType) {
        return new GenericBeanClass<>(genericType);
    }

    @Override
    public Optional<BeanClass<?>> getTypeArguments(int position) {
        return genericType.getGenericTypeParameter(position).map(BeanClass::create);
    }

    @Override
    public boolean hasTypeArguments() {
        return genericType.hasTypeArguments();
    }
}
