package com.github.leeonky.util.function;

import java.util.Objects;
import java.util.function.Predicate;

public class Extension {

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> not(Predicate<? extends T> t) {
        Objects.requireNonNull(t);
        return (Predicate<T>) t.negate();
    }
}
