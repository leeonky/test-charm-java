package org.testcharm.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Pair<T1, T2> {
    private final T1 first;
    private final T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public static <T1, T2> Pair<T1, T2> pair(T1 first, T2 second) {
        return new Pair<>(first, second);
    }

    public static <T> Same<T> same(T first, T second) {
        return new Same<>(first, second);
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    public static class Same<T> extends Pair<T, T> {
        public Same(T first, T second) {
            super(first, second);
        }

        public <R, X> Optional<X> both(Function<T, Optional<R>> mapper, BiFunction<R, R, X> mapper2) {
            return mapper.apply(getFirst()).flatMap(v1 -> mapper.apply(getSecond()).map(v2 -> mapper2.apply(v1, v2)));
        }

        public <R, X> X map(Function<T, R> mapper, BiFunction<R, R, X> mapper2) {
            return mapper2.apply(mapper.apply(getFirst()), mapper.apply(getSecond()));
        }
    }
}
