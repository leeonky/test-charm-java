package com.github.leeonky.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PairTest {

    @Test
    void create_pair() {
        Pair<String> p = Pair.pair("hello", "world");

        assertThat(p.getFirst()).isEqualTo("hello");
        assertThat(p.getSecond()).isEqualTo("world");
    }

    @Nested
    class Both {

        @Test
        void both_has_value() {
            Pair<String> p = Pair.pair("hello", "world");

            Optional<String> r = p.both(Optional::of, (s1, s2) -> s1 + s2);

            assertThat(r).hasValue("helloworld");
        }

        @Test
        void first_has_value() {
            Pair<String> p = Pair.pair("hello", null);

            Optional<String> r = p.both(Optional::ofNullable, (s1, s2) -> s1 + s2);

            assertThat(r).isEmpty();
        }

        @Test
        void second_has_value() {
            Pair<String> p = Pair.pair(null, "world");

            Optional<String> r = p.both(Optional::ofNullable, (s1, s2) -> s1 + s2);

            assertThat(r).isEmpty();
        }

        @Test
        void both_has_no_value() {
            Pair<String> p = Pair.pair(null, null);

            Optional<String> r = p.both(Optional::ofNullable, (s1, s2) -> s1 + s2);

            assertThat(r).isEmpty();
        }
    }
}