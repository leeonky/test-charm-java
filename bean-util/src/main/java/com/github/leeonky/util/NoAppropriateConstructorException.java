package com.github.leeonky.util;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NoAppropriateConstructorException extends IllegalArgumentException {
    public NoAppropriateConstructorException(Class<?> type, Object... args) {
        super(String.format("No appropriate %s constructor for params [%s]",
                type.getName(), toString(args)));
    }

    private static String toString(Object[] parameters) {
        return Stream.of(parameters)
                .map(o -> o == null ? "null" : o.getClass().getName() + ":" + o)
                .collect(Collectors.joining(", "));
    }
}
