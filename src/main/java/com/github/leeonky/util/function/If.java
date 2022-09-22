package com.github.leeonky.util.function;

import java.util.Optional;
import java.util.function.Supplier;

public interface If {
    boolean is();

    default <T> Optional<T> optional(Supplier<T> factory) {
        if (is())
            return Optional.ofNullable(factory.get());
        return Optional.empty();
    }
}
