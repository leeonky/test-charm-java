package com.github.leeonky.util.function;

import java.util.Optional;
import java.util.function.Supplier;

public interface If {

    @SafeVarargs
    static <T> T firstNonNull(T... inputs) {
        for (T other : inputs) {
            if (other != null)
                return other;
        }
        return null;
    }

    boolean is();

    default <T> Optional<T> optional(Supplier<T> factory) {
        if (is())
            return Optional.ofNullable(factory.get());
        return Optional.empty();
    }
}
