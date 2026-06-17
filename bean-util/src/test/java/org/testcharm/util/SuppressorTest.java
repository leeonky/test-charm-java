package org.testcharm.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testcharm.dal.Assertions.expect;

class SuppressorTest {

    @Nested
    class LastEqualsCase {

        @Test
        void last_status_equals() {
            Suppressor.assertLastEqualsCase("actual", "actual"); // should not throw
        }

        @Test
        void last_status_not_equals() {
            expect(assertThrows(IllegalStateException.class, () -> Suppressor.assertLastEqualsCase("actual", "expected")).getMessage())
                    .isEqualTo("Unexpected case in fallback branch: expected <expected> but got <actual> - this may indicate a new case type was added but not handled in the condition chain");
        }
    }

    @Nested
    class GetIgnoring {

        @Test
        void get_and_return() {
            String result = Suppressor.getIgnoring(() -> "success", "default");
            expect(result).isEqualTo("success");
        }

        @Test
        void get_ignoring_with_exception_and_return_default_value() {
            String result = Suppressor.getIgnoring(() -> {
                throw new RuntimeException("fail");
            }, "default");
            expect(result).isEqualTo("default");
        }
    }

    @Nested
    class RunIgnoring {

        @SneakyThrows
        @Test
        void run_ignoring_no_exception() {
            ThrowingRunnable runnable = mock(ThrowingRunnable.class);
            Suppressor.runIgnoring(runnable);

            verify(runnable).run();
        }

        @Test
        void run_ignoring_with_exception() {
            Suppressor.runIgnoring(() -> {
                throw new RuntimeException("fail"); // should be ignored
            });
        }
    }
}