package com.github.leeonky.dal.extensions.basic.formatters;

import com.github.leeonky.dal.extensions.basic.hex.util.Hex;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.github.leeonky.dal.extensions.basic.hex.util.Hex.hex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HexTest {

    @Nested
    class ConstructFromText {

        @Test
        void empty() {
            assertThat(hex("")).isEqualTo(new Hex(new byte[]{}));
        }

        @Test
        void trim_blank() {
            assertThat(hex(" \t\n")).isEqualTo(new Hex(new byte[]{}));
        }

        @Test
        void one_byte() {
            assertThat(hex("0A")).isEqualTo(new Hex(new byte[]{0xA}));
            assertThat(hex("AB")).isEqualTo(new Hex(new byte[]{(byte) 0xAB}));
        }

        @Test
        void two_bytes() {
            assertThat(hex("0A ab")).isEqualTo(new Hex(new byte[]{0xA, (byte) 0xAB}));
        }

        @Test
        void optional_comma() {
            assertThat(hex("0A, ab")).isEqualTo(new Hex(new byte[]{0xA, (byte) 0xAB}));
        }

        @Test
        void invalid_char() {
            assertThatThrownBy(() -> hex("A")).hasMessageContaining("incomplete byte: A, each byte should has 2 hex numbers");
        }
    }

    @Nested
    class ToString {

        @Test
        void empty() {
            assertThat(hex("").toString()).isEqualTo("empty");
        }


        @Test
        void one_byte() {
            assertThat(hex("61").toString()).isEqualTo("size 1\n" +
                    "00000000: 61                                                 a");
        }

        @Test
        void one_line_bytes() {
            assertThat(hex("61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70").toString()).isEqualTo("size 16\n" +
                    "00000000: 61 62 63 64  65 66 67 68  69 6A 6B 6C  6D 6E 6F 70 abcdefghijklmnop");
        }

        @Test
        void tow_line_bytes() {
            assertThat(hex("61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70 71").toString()).isEqualTo("size 17\n" +
                    "00000000: 61 62 63 64  65 66 67 68  69 6A 6B 6C  6D 6E 6F 70 abcdefghijklmnop\n" +
                    "00000010: 71                                                 q");
        }

        @Test
        void one_invalid_code_point_byte() {
            assertThat(hex("FF").toString()).isEqualTo("size 1\n" +
                    "00000000: FF                                                 .");
        }
    }
}