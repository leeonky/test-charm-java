package com.github.leeonky.util.function;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ExtensionTest {

    @Nested
    class Not {

        @Test
        void not_predicate() {
            Predicate<String> predicate = s -> s.equals("true");

            assertThat(Extension.not(predicate).test("true")).isFalse();
            assertThat(Extension.not(predicate).test("not true")).isTrue();
        }
    }
}