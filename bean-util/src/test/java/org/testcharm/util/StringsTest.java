package org.testcharm.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringsTest {

    @Nested
    class NullOrEmpty {

        @Test
        void nullString() {
            assertTrue(Strings.nullOrEmpty(null));
        }

        @Test
        void emptyString() {
            assertTrue(Strings.nullOrEmpty(""));
        }

        @Test
        void nonEmptyString() {
            assertFalse(Strings.nullOrEmpty("test"));
        }
    }
}