package com.github.leeonky.util.function;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.stream.Stream.of;

public class Extension {

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> not(Predicate<? extends T> t) {
        Objects.requireNonNull(t);
        return (Predicate<T>) t.negate();
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getFirstPresent(Supplier<Optional<? extends T>>... optionals) {
        return (Optional<T>) of(optionals).map(Supplier::get).filter(Optional::isPresent).findFirst().orElse(empty());
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> firstPresent(Optional<? extends T>... optionals) {
        return (Optional<T>) of(optionals).filter(Optional::isPresent).findFirst().orElse(empty());
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
}
