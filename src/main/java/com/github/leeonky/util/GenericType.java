package com.github.leeonky.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class GenericType {
    private final Type type;

    private GenericType(Type type) {
        this.type = type;
    }

    public static GenericType createGenericType(Type type) {
        return new GenericType(type);
    }

    public Class<?> getRawType() {
        if (type instanceof ParameterizedType)
            return (Class<?>) ((ParameterizedType) type).getRawType();
        return (Class<?>) type;
    }

    public Optional<GenericType> getGenericTypeParameter(int i) {
        if (type instanceof ParameterizedType) {
            Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[i];
            if (typeArgument instanceof Class || typeArgument instanceof ParameterizedType)
                return Optional.of(new GenericType(typeArgument));
        }
        return Optional.empty();
    }
}
