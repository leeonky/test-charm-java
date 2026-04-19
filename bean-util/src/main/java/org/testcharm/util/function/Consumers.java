package org.testcharm.util.function;

import java.util.function.Consumer;

public class Consumers {

    private static final Consumer<Object> NO_OP = t -> {
    };

    @SuppressWarnings("unchecked")
    public static <T> Consumer<T> noop() {
        return (Consumer<T>) NO_OP;
    }
}
