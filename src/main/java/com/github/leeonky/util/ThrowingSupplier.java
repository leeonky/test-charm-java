package com.github.leeonky.util;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Throwable;
}
