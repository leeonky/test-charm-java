package org.testcharm.util.function;

public class Consumer {

    private static final java.util.function.Consumer<Object> NO_OP = t -> {
    };

    @SuppressWarnings("unchecked")
    public static <T> java.util.function.Consumer<T> noop() {
        return (java.util.function.Consumer<T>) NO_OP;
    }
}
