package org.testcharm.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberFormatTest {
    NumberParser numberParser = new NumberParser();

    @Nested
    class Format {

        @Nested
        class Radix2 {

            @Nested
            class FormatByte {

                @Test
                void signed_byte_min_max() {
                    test_format("0B1111111y", Byte.MAX_VALUE, 1, 2, "0b1111111");
                    test_format("-0B10000000y", Byte.MIN_VALUE, -1, 2, "-0b10000000");
                }

                @Test
                void unsigned_byte_min_max() {
                    test_format("0B11111111y", (byte) -1, 1, 2, "0b11111111");
                    test_format("0B0y", (byte) 0, 1, 2, "0b0");
                }
            }

            @Nested
            class FormatShort {

                @Test
                void signed_short_min_max() {
                    test_format("0B111111111111111s", Short.MAX_VALUE, 1, 2, "0b111111111111111");
                    test_format("-0B1000000000000000s", Short.MIN_VALUE, -1, 2, "-0b1000000000000000");
                }

                @Test
                void unsigned_short_min_max() {
                    test_format("0B1111111111111111s", (short) -1, 1, 2, "0b1111111111111111");
                    test_format("0B0s", (short) 0, 1, 2, "0b0");
                }
            }

            @Nested
            class FormatInt {

                @Test
                void signed_int_min_max() {
                    test_format("0B1111111111111111111111111111111", Integer.MAX_VALUE, 1, 2, "0b1111111111111111111111111111111");
                    test_format("-0B10000000000000000000000000000000", Integer.MIN_VALUE, -1, 2, "-0b10000000000000000000000000000000");
                }

                @Test
                void unsigned_int_min_max() {
                    test_format("0B11111111111111111111111111111111", -1, 1, 2, "0b11111111111111111111111111111111");
                    test_format("0B0", 0, 1, 2, "0b0");
                }
            }
        }

        @Nested
        class Radix16 {

            @Nested
            class FormatByte {

                @Test
                void signed_byte_min_max() {
                    test_format("0x7fy", Byte.MAX_VALUE, 1, 16, "0x7F");
                    test_format("-0x80y", Byte.MIN_VALUE, -1, 16, "-0x80");
                }

                @Test
                void unsigned_byte_min_max() {
                    test_format("0xffy", (byte) -1, 1, 16, "0xFF");
                    test_format("0x0y", (byte) 0, 1, 16, "0x0");
                }
            }

            @Nested
            class FormatShort {

                @Test
                void signed_short_min_max() {
                    test_format("0x7fffs", Short.MAX_VALUE, 1, 16, "0x7FFF");
                    test_format("-0x8000s", Short.MIN_VALUE, -1, 16, "-0x8000");
                }

                @Test
                void unsigned_short_min_max() {
                    test_format("0xffffs", (short) -1, 1, 16, "0xFFFF");
                    test_format("0x0s", (short) 0, 1, 16, "0x0");
                }
            }

            @Nested
            class FormatInt {

                @Test
                void signed_int_min_max() {
                    test_format("0x7fffffff", Integer.MAX_VALUE, 1, 16, "0x7FFFFFFF");
                    test_format("-0x80000000", Integer.MIN_VALUE, -1, 16, "-0x80000000");
                }

                @Test
                void unsigned_int_min_max() {
                    test_format("0xffffffff", -1, 1, 16, "0xFFFFFFFF");
                    test_format("0x0", 0, 1, 16, "0x0");
                }
            }
        }

        @Nested
        class Radix8 {

            @Nested
            class FormatByte {

                @Test
                void signed_byte_min_max() {
                    test_format("0177y", Byte.MAX_VALUE, 1, 8, "0177");
                    test_format("-0200y", Byte.MIN_VALUE, -1, 8, "-0200");
                }

                @Test
                void unsigned_byte_min_max() {
                    test_format("0377y", (byte) -1, 1, 8, "0377");
                    test_format("00y", (byte) 0, 1, 8, "00");
                }
            }

            @Nested
            class FormatShort {

                @Test
                void signed_short_min_max() {
                    test_format("077777s", Short.MAX_VALUE, 1, 8, "077777");
                    test_format("-0100000s", Short.MIN_VALUE, -1, 8, "-0100000");
                }

                @Test
                void unsigned_short_min_max() {
                    test_format("0177777s", (short) -1, 1, 8, "0177777");
                    test_format("00s", (short) 0, 1, 8, "00");
                }
            }

            @Nested
            class FormatInt {

                @Test
                void signed_int_min_max() {
                    test_format("017777777777", Integer.MAX_VALUE, 1, 8, "017777777777");
                    test_format("-020000000000", Integer.MIN_VALUE, -1, 8, "-020000000000");
                }

                @Test
                void unsigned_int_min_max() {
                    test_format("037777777777", -1, 1, 8, "037777777777");
                    test_format("00", 0, 1, 8, "00");
                }
            }
        }

        @Nested
        class Radix10 {

            @Test
            void signed_byte_min_max() {
                test_format("127y", Byte.MAX_VALUE, 1, 10, "127");
                test_format("-128y", Byte.MIN_VALUE, -1, 10, "-128");
            }

            @Test
            void signed_short_min_max() {
                test_format("32767s", Short.MAX_VALUE, 1, 10, "32767");
                test_format("-32768s", Short.MIN_VALUE, -1, 10, "-32768");
            }

            @Test
            void signed_int_min_max() {
                test_format("2147483647", Integer.MAX_VALUE, 1, 10, "2147483647");
                test_format("-2147483648", Integer.MIN_VALUE, -1, 10, "-2147483648");
            }
        }
    }

    private void test_format(String content, Number number, int sign, int radix, String expect) {
        NumberWithFormat numberWithFormat = numberParser.parse(content);
        assertEquals(number, numberWithFormat.number);
        assertEquals(sign, numberWithFormat.format.sign);
        assertEquals(radix, numberWithFormat.format.radix);
        assertEquals(expect, numberWithFormat.format.format(numberWithFormat.number));
    }
}