package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.extensions.basic.binary.util.HexDumper;
import com.github.leeonky.dal.extensions.basic.binary.util.HexFormatter;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HexTest {

    private byte[] parseToByteArray(String hexInText) {
        return new HexFormatter().format(hexInText, null);
    }

    @Nested
    class ConstructFromText {

        @Test
        void empty() {
            assertThat(parseToByteArray("")).isEqualTo(new byte[]{});
        }

        @Test
        void trim_blank() {
            assertThat(parseToByteArray(" \t\n")).isEqualTo(new byte[]{});
        }

        @Test
        void one_byte() {
            assertThat(parseToByteArray("0A")).isEqualTo(new byte[]{0xA});
            assertThat(parseToByteArray("AB")).isEqualTo(new byte[]{(byte) 0xAB});
        }

        @Test
        void two_bytes() {
            assertThat(parseToByteArray("0A ab")).isEqualTo(new byte[]{0xA, (byte) 0xAB});
        }

        @Test
        void optional_comma() {
            assertThat(parseToByteArray("0A, ab")).isEqualTo(new byte[]{0xA, (byte) 0xAB});
        }

        @Test
        void invalid_char() {
            assertThatThrownBy(() -> parseToByteArray("A")).hasMessageContaining("incomplete byte: A, each byte should has 2 hex numbers");
        }
    }

    @Nested
    class ToString {

        @Test
        void empty() {
            assertThat(parseAndDump("")).isEqualTo("Empty binary");
        }

        private String parseAndDump(String hexInText) {
            RuntimeContextBuilder.DALRuntimeContext context = new RuntimeContextBuilder().build(parseToByteArray(hexInText));
            DumpingBuffer dumpingBuffer = DumpingBuffer.rootContext(context);
            new HexDumper().dumpValue(context.getThis(), dumpingBuffer);
            return dumpingBuffer.content();
        }


        @Test
        void one_byte() {
            assertThat(parseAndDump("61")).isEqualTo("Binary size 1\n" +
                    "00000000: 61                                                 a");
        }

        @Test
        void one_line_bytes() {
            assertThat(parseAndDump("61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70")).isEqualTo("Binary size 16\n" +
                    "00000000: 61 62 63 64  65 66 67 68  69 6A 6B 6C  6D 6E 6F 70 abcdefghijklmnop");
        }

        @Test
        void tow_line_bytes() {
            assertThat(parseAndDump("61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 70 71")).isEqualTo("Binary size 17\n" +
                    "00000000: 61 62 63 64  65 66 67 68  69 6A 6B 6C  6D 6E 6F 70 abcdefghijklmnop\n" +
                    "00000010: 71                                                 q");
        }

        @Test
        void one_invalid_code_point_byte() {
            assertThat(parseAndDump("FF")).isEqualTo("Binary size 1\n" +
                    "00000000: FF                                                 .");
        }
    }
}