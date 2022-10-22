package com.github.leeonky.dal.extensions.formatters;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HexTest {

    @Nested
    class ConstructFromText {

        @Test
        void empty() {
            assertThat(Hex.hex("")).isEqualTo(new Hex(new byte[]{}));
        }

        @Test
        void trim_blank() {
            assertThat(Hex.hex(" \t\n")).isEqualTo(new Hex(new byte[]{}));
        }

        @Test
        void one_byte() {
            assertThat(Hex.hex("0A")).isEqualTo(new Hex(new byte[]{0xA}));
            assertThat(Hex.hex("AB")).isEqualTo(new Hex(new byte[]{(byte) 0xAB}));
        }

        @Test
        void two_bytes() {
            assertThat(Hex.hex("0A ab")).isEqualTo(new Hex(new byte[]{0xA, (byte) 0xAB}));
        }

        @Test
        void optional_comma() {
            assertThat(Hex.hex("0A, ab")).isEqualTo(new Hex(new byte[]{0xA, (byte) 0xAB}));
        }

        @Test
        void invalid_char() {
            assertThatThrownBy(() -> Hex.hex("A")).hasMessageContaining("incomplete byte: A, each byte should has 2 hex numbers");
        }
    }
}