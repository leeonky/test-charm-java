package com.github.leeonky.util.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;

public class Extension {

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> not(Predicate<? extends T> t) {
        Objects.requireNonNull(t);
        return (Predicate<T>) t.negate();
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getFirstPresent(Supplier<? extends Optional<? extends T>>... optionals) {
        return (Optional<T>) of(optionals).map(Supplier::get).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> firstPresent(Optional<? extends T>... optionals) {
        return (Optional<T>) of(optionals).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> firstPresent(Stream<Optional<? extends T>> optionals) {
        return (Optional<T>) optionals.filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    public static <T> BinaryOperator<T> notAllowParallelReduce() {
        return (o1, o2) -> {
            throw new IllegalStateException("Not allow parallel here!");
        };
    }

    public static <E, R> Optional<R> mapFirst(Collection<? extends E> collection, Function<E, Optional<R>> mapper) {
        return collection.stream().map(mapper).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    public static <E, R> Optional<R> mapFirst(E[] collection, Function<E, Optional<R>> mapper) {
        return Arrays.stream(collection).map(mapper).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    public static <E, R> Stream<R> mapPresent(Collection<? extends E> collection, Function<E, Optional<R>> mapper) {
        return collection.stream().map(mapper).filter(Optional::isPresent).map(Optional::get);
    }
}
