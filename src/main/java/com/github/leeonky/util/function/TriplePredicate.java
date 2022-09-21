package com.github.leeonky.util.function;

@FunctionalInterface
public interface TriplePredicate<T, U, V> {
    boolean test(T t, U u, V v);
}
