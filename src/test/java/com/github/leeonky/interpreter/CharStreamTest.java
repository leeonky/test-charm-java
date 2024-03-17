package com.github.leeonky.interpreter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CharStreamTest {

    @Nested
    class NonBlankBetween {

        @Test
        void no_blank() {
            CharStream charStream = new CharStream("ABC");

            assertThat(charStream.firstNonBlankBetween(0, 2)).isEqualTo(-1);
        }

        @Test
        void first_position_not_blank_with_one_blank() {
            CharStream charStream = new CharStream("AB C");

            assertThat(charStream.firstNonBlankBetween(0, 3)).isEqualTo(1);
        }

        @Test
        void first_position_not_blank_with_two_blanks() {
            CharStream charStream = new CharStream("A  C");

            assertThat(charStream.firstNonBlankBetween(0, 3)).isEqualTo(0);
        }

        @Test
        void first_position_not_blank_with_three_blanks() {
            CharStream charStream = new CharStream("A   C");

            assertThat(charStream.firstNonBlankBetween(0, 4)).isEqualTo(0);
        }

        @Test
        void first_position_is_blank() {
            CharStream charStream = new CharStream("  C");

            assertThat(charStream.firstNonBlankBetween(0, 2)).isEqualTo(-1);
        }

        @Test
        void first_position_is_blank_and_not_from_start() {
            CharStream charStream = new CharStream("   C");

            assertThat(charStream.firstNonBlankBetween(1, 3)).isEqualTo(0);
        }

        @Nested
        class InvalidPosition {

            @Test
            void second_less_than_1() {
                CharStream charStream = new CharStream("  ");

                assertThat(charStream.firstNonBlankBetween(0, 0)).isEqualTo(-1);
            }

            @Test
            void first_less_than_zero() {
                CharStream charStream = new CharStream("  ");

                assertThat(charStream.firstNonBlankBetween(-1, 2)).isEqualTo(-1);
            }

            @Test
            void first_should_less_than_second() {
                CharStream charStream = new CharStream("   ");

                assertThat(charStream.firstNonBlankBetween(2, 2)).isEqualTo(-1);
                assertThat(charStream.firstNonBlankBetween(3, 2)).isEqualTo(-1);
            }

            @Test
            void second_out_of_string_index() {
                CharStream charStream = new CharStream("  ");

                assertThat(charStream.firstNonBlankBetween(0, 4)).isEqualTo(-1);
            }
        }
    }
}