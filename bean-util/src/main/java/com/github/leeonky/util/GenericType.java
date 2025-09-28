package com.github.leeonky.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GenericType {
    private final static Map<Type, GenericType> instanceCache = new ConcurrentHashMap<>();
    private final Type type;

    private GenericType(Type type) {
        this.type = Objects.requireNonNull(type);
    }

    public static GenericType createGenericType(Type type) {
        return instanceCache.computeIfAbsent(type, GenericType::new);
    }

    public Class<?> getRawType() {
        if (type instanceof ParameterizedType)
            return (Class<?>) ((ParameterizedType) type).getRawType();
        if (type instanceof TypeVariable)
            return Object.class;
        return (Class<?>) type;
    }

    public Optional<GenericType> getGenericTypeParameter(int parameterIndex) {
        if (type instanceof ParameterizedType) {
            Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[parameterIndex];
            if (typeArgument instanceof Class || typeArgument instanceof ParameterizedType)
                return Optional.of(new GenericType(typeArgument));
        }
        return Optional.empty();
    }

    public boolean hasTypeArguments() {
        return type instanceof ParameterizedType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(GenericType.class, type);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GenericType && Objects.equals(type, ((GenericType) obj).type);
    }

    public Type getGenericType() {
        return type;
    }
}
