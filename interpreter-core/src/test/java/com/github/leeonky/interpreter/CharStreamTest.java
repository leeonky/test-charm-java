package com.github.leeonky.interpreter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CharStreamTest {

    @Nested
    class NewlineBetween {
        @Nested
        class InvalidPosition {

            @Test
            void right_less_than_1() {
                CharStream charStream = new CharStream("  ");

                assertThat(charStream.newlineBetween(0, 0)).isEqualTo(-1);
            }

            @Test
            void first_less_than_zero() {
                CharStream charStream = new CharStream("  ");

                assertThat(charStream.newlineBetween(-1, 2)).isEqualTo(-1);
            }

            @Test
            void first_should_less_than_second() {
                CharStream charStream = new CharStream("   ");

                assertThat(charStream.newlineBetween(2, 2)).isEqualTo(-1);
                assertThat(charStream.newlineBetween(3, 2)).isEqualTo(-1);
            }

            @Test
            void second_out_of_string_index() {
                CharStream charStream = new CharStream("  ");

                assertThat(charStream.newlineBetween(0, 4)).isEqualTo(-1);
            }
        }

        @Test
        void no_newline() {
            CharStream charStream = new CharStream("ABC");

            assertThat(charStream.newlineBetween(0, 2)).isEqualTo(-1);
        }

        @Test
        void find_one_newline_before_second() {
            CharStream charStream = new CharStream("AB\nC");

            assertThat(charStream.newlineBetween(0, 3)).isEqualTo(1);
        }

        @Test
        void find_more_newlines_before_second() {
            CharStream charStream = new CharStream("AB\n\n\nC");

            assertThat(charStream.newlineBetween(0, 5)).isEqualTo(1);
        }

        @Nested
        class NewlineAndBlank {
            @Test
            void blank_after_newline() {
                CharStream charStream = new CharStream("AB\n C");

                assertThat(charStream.newlineBetween(0, 4)).isEqualTo(1);
            }


            @Test
            void supported_blank_after_newline() {
                CharStream charStream = new CharStream("AB\n \t\bC");

                assertThat(charStream.newlineBetween(0, 6)).isEqualTo(1);
            }

            @Test
            void should_not_raise_exception_when_no_newline() {
                CharStream charStream = new CharStream("  C");

                assertThat(charStream.newlineBetween(0, 2)).isEqualTo(-1);
            }
        }

        @Test
        void first_position_is_newline() {
            CharStream charStream = new CharStream("\n\nC");

            assertThat(charStream.newlineBetween(0, 2)).isEqualTo(-1);
        }

        @Test
        void first_position_is_newline_and_first_not_from_start() {
            CharStream charStream = new CharStream("\n\n\nC");

            assertThat(charStream.newlineBetween(1, 3)).isEqualTo(-1);
        }

        @Test
        void supported_newline_chars() {
            CharStream charStream = new CharStream("xA\n\rC");

            assertThat(charStream.newlineBetween(0, 4)).isEqualTo(1);
        }

        @Test
        void multiple_newline_and_blank() {
            CharStream charStream = new CharStream("xA\n \n C");

            assertThat(charStream.newlineBetween(0, 6)).isEqualTo(1);
        }
    }
}