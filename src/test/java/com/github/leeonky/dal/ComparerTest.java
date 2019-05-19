package com.github.leeonky.dal;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ComparerTest {
    private void assertCompare(Object v1, Object v2, int result) {
        assertThat(Comparer.compare(v1, v2)).isEqualTo(result);
    }

    @Nested
    class NumberCompare {

        @Test
        void all_params_should_not_be_null() {
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> Comparer.compare(1, null));
            assertThat(illegalStateException).hasMessage("Can not compare <1> and <null>");

            illegalStateException = assertThrows(IllegalStateException.class, () -> Comparer.compare(null, null));
            assertThat(illegalStateException).hasMessage("Can not compare <null> and <null>");

            illegalStateException = assertThrows(IllegalStateException.class, () -> Comparer.compare(null, 1));
            assertThat(illegalStateException).hasMessage("Can not compare <null> and <1>");
        }

        @Test
        void compare_number_in_different_type() {
            assertCompare(1, Integer.valueOf(1), 0);
            assertCompare(1, Byte.valueOf((byte) 1), 0);
            assertCompare(1, Short.valueOf((byte) 1), 0);
            assertCompare(1, Long.valueOf((byte) 1), 0);
            assertCompare(1, new BigDecimal(1), 0);
            assertCompare(1, new BigInteger("1"), 0);
            assertCompare(1, 0, 1);
        }

        @Test
        void should_check_type_before_compare() {
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> Comparer.compare(1, "1"));
            assertThat(illegalStateException).hasMessage("Can not compare <java.lang.Integer: 1> and <java.lang.String: 1>");
        }
    }

    @Nested
    class StringCompare {

        @Test
        void compare_string() {
            assertCompare("a", "a", 0);
            assertCompare("b", "a", 1);
        }
    }

    @Nested
    class Equal {
        @Test
        void both_null_is_equal() {
            assertTrue(Comparer.equals(null, null));
        }

        @Test
        void null_is_not_equal_to_not_null() {
            assertFalse(Comparer.equals(null, 1));
            assertFalse(Comparer.equals(1, null));
        }

        @Test
        void number_equal() {
            assertTrue(Comparer.equals(1, 1L));
        }

        @Test
        void other_type_equal() {
            assertTrue(Comparer.equals("a", "a"));
        }
    }
}