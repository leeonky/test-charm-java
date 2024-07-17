package com.github.leeonky.util;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.stream.Stream.concat;

public class TypeHandlerSet<T> {
    private final Map<Class<?>, List<TypeHandler<T>>> handlers = new HashMap<>();

    public void add(Class<?> source, Class<?> target, T converter) {
        List<TypeHandler<T>> typeHandlers = handlers.computeIfAbsent(target, k -> new ArrayList<>());
        typeHandlers.removeIf(handler -> handler.isPreciseType(source));
        typeHandlers.add(new TypeHandler<>(source, converter));
        typeHandlers.sort(TypeHandler::sortClass);
    }

    public Optional<TypeHandler<T>> findHandler(Class<?> source, Class<?> target) {
        List<TypeHandler<T>> converters = handlers.get(target);
        if (converters == null)
            converters = findHandlersBySubType(target);
        return concat(converters.stream().filter(t -> t.isPreciseType(source)),
                converters.stream().filter(t -> t.isBaseType(source))).findFirst();
    }

    private List<TypeHandler<T>> findHandlersBySubType(Class<?> target) {
        return handlers.entrySet().stream()
                .filter(e -> e.getKey().isAssignableFrom(target))
                .map(Map.Entry::getValue)
                .findFirst().orElse(emptyList());
    }
}
