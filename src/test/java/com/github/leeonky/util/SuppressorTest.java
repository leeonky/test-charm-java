package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SuppressorTest {

    @Nested
    class InvokeReturn {

        @Test
        void return_from_code_block() {
            assertThat(Suppressor.get(() -> 1)).isEqualTo(1);
        }

        @Test
        void should_throw_runtime_exception_directly() {
            RuntimeException runtimeException = new RuntimeException();
            assertThat(assertThrows(Exception.class, () -> Suppressor.get(() -> {
                throw runtimeException;
            }))).isEqualTo(runtimeException);
        }

        @Test
        void should_re_throw_checked_exception() {
            Exception exception = new Exception();
            assertThat(assertThrows(IllegalStateException.class, () -> Suppressor.get(() -> {
                throw exception;
            })).getCause()).isEqualTo(exception);
        }

        @Test
        void should_re_throw_with_target_exception_when_got_invocation_target_exception() {
            Exception exception = new Exception();
            assertThat(assertThrows(IllegalStateException.class, () -> Suppressor.get(() -> {
                throw new InvocationTargetException(exception);
            })).getCause()).isEqualTo(exception);
        }
    }

    @Nested
    class InvokeRun {
        private int value = 0;

        @Test
        void return_invoke_code_block() {
            Suppressor.run(() -> value = 1);

            assertThat(value).isEqualTo(1);
        }

        @Test
        void should_throw_runtime_exception_directly() {
            RuntimeException runtimeException = new RuntimeException();
            assertThat(assertThrows(Exception.class, () -> Suppressor.run(() -> {
                throw runtimeException;
            }))).isEqualTo(runtimeException);
        }

        @Test
        void should_re_throw_checked_exception() {
            Exception exception = new Exception();
            assertThat(assertThrows(IllegalStateException.class, () -> Suppressor.run(() -> {
                throw exception;
            })).getCause()).isEqualTo(exception);
        }

        @Test
        void should_re_throw_with_target_exception_when_got_invocation_target_exception() {
            Exception exception = new Exception();
            assertThat(assertThrows(IllegalStateException.class, () -> Suppressor.run(() -> {
                throw new InvocationTargetException(exception);
            })).getCause()).isEqualTo(exception);
        }
    }
}