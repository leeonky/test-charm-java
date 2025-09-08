package com.github.leeonky.util;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GenericBeanClass<T> extends BeanClass<T> {
    private final static Map<GenericType, GenericBeanClass<?>> instanceCache = new ConcurrentHashMap<>();
    private final GenericType genericType;

    @SuppressWarnings("unchecked")
    protected GenericBeanClass(GenericType genericType) {
        super((Class<T>) genericType.getRawType());
        this.genericType = genericType;
    }

    public static BeanClass<?> create(GenericType genericType) {
        return instanceCache.computeIfAbsent(genericType, GenericBeanClass::new);
    }

    @Override
    public Optional<BeanClass<?>> getTypeArguments(int position) {
        return genericType.getGenericTypeParameter(position).map(BeanClass::create);
    }

    @Override
    public boolean hasTypeArguments() {
        return genericType.hasTypeArguments();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GenericBeanClass.class, genericType);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GenericBeanClass && Objects.equals(genericType, ((GenericBeanClass) obj).genericType);
    }

    @Override
    public Type getGenericType() {
        return genericType.getGenericType();
    }
}
