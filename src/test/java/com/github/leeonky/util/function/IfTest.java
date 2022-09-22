package com.github.leeonky.util.function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IfTest {

    @Nested
    class Optional {

        @Test
        void return_empty_when_if_false() {
            assertThat(When.when(false).optional(null)).isEmpty();
        }

        @Test
        void return_empty_when_if_true() {
            assertThat(When.when(true).optional(() -> "result")).hasValue("result");
        }
    }
}
