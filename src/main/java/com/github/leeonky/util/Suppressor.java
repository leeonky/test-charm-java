package com.github.leeonky.util;

import java.lang.reflect.InvocationTargetException;

public class Suppressor {
    public static <T> T get(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getTargetException());
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    public static void run(ThrowingRunnable run) {
        get(() -> {
            run.run();
            return null;
        });
    }
}
