package com.github.leeonky.util.function;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static java.util.stream.Stream.of;

public class Extension {

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> not(Predicate<? extends T> t) {
        Objects.requireNonNull(t);
        return (Predicate<T>) t.negate();
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> Optional<T> oneOf(Supplier<Optional<? extends T>>... optionals) {
        return (Optional<T>) of(optionals).map(Supplier::get).filter(Optional::isPresent).findFirst().orElse(empty());
    }

    public static <T> BinaryOperator<T> notAllowParallelReduce() {
        return (o1, o2) -> {
            throw new IllegalStateException("Not allow parallel here!");
        };
    }
}
