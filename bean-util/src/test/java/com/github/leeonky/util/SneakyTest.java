package com.github.leeonky.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SneakyTest {

    @Test
    void return_from_code_block() {
        assertThat(Sneaky.get(() -> 1)).isEqualTo(1);
    }

    @Test
    void throw_exception() {
        Exception exception = new Exception();
        assertThat(assertThrows(Exception.class, () -> Sneaky.get(() -> {
            throw exception;
        }))).isEqualTo(exception);
    }


    private boolean called = false;

    @Test
    void run_return_void() {
        Sneaky.run(() -> called = true);

        assertThat(called).isTrue();
    }

    @Test
    void run_throw_exception() {
        Exception exception = new Exception();
        assertThat(assertThrows(Exception.class, () -> Sneaky.run(() -> {
            throw exception;
        }))).isEqualTo(exception);
    }

    @Test
    void execute_should_catch_InvocationTargetException_and_rethrow() {
        Exception exception = new Exception();
        assertThat(assertThrows(Exception.class, () -> Sneaky.execute(() -> {
            throw new InvocationTargetException(exception);
        }))).isEqualTo(exception);
    }

    @Test
    void execute_void_should_catch_InvocationTargetException_and_rethrow() {
        Exception exception = new Exception();
        assertThat(assertThrows(Exception.class, () -> Sneaky.executeVoid(() -> {
            throw new InvocationTargetException(exception);
        }))).isEqualTo(exception);
    }
}
