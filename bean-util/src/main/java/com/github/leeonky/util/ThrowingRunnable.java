package com.github.leeonky.util;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Throwable;
}
