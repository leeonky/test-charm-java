package com.github.leeonky.util;

import java.lang.reflect.InvocationTargetException;

public class Sneaky {
    public static <T> T get(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return sneakyThrow(e);
        }
    }

    public static <T> T execute(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (InvocationTargetException e) {
            return sneakyThrow(e.getTargetException());
        } catch (Throwable e) {
            return sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends Throwable, T> T sneakyThrow(Throwable throwable) throws E {
        throw (E) throwable;
    }

    public static void run(ThrowingRunnable runnable) {
        get(() -> {
            runnable.run();
            return null;
        });
    }
}
