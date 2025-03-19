package com.github.leeonky.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Pair<T> {
    private final T first;
    private final T second;

    public Pair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public static <T> Pair<T> pair(T first, T second) {
        return new Pair<>(first, second);
    }

    public T getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public <R, X> Optional<X> both(Function<T, Optional<R>> mapper, BiFunction<R, R, X> mapper2) {
        return mapper.apply(first).flatMap(v1 -> mapper.apply(second).map(v2 -> mapper2.apply(v1, v2)));
    }

    public <R, X> X map(Function<T, R> mapper, BiFunction<R, R, X> mapper2) {
        return mapper2.apply(mapper.apply(first), mapper.apply(second));
    }
}
