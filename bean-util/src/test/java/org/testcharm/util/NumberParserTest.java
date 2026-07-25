package org.testcharm.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumberParserTest {

    @Test
    void invalid_number() {
        assertParse("+", null);
        assertParse("-", null);
        assertParse("1_", null);
        assertParse("", null);
        assertParse("notNumber", null);
        assertParse("+-1", null);
    }

    private void assertParseOverflow(String code) {
        assertThat(assertThrows(NumberOverflowException.class, () -> new NumberParser().parse(code)))
                .hasMessageContaining(String.format("Cannon save [%s] with the given postfix type", code));
    }

    private void assertParse(String inputCode, Number expected) {
        assertThat(new NumberParser().parse(inputCode)).isEqualTo(expected);
    }

    @Test
    void bug_for_parse_number() {
        assertParse(":;<=>?@", null);
    }

    @Nested
    class ParseFloat {

        @Test
        void power_number_in_double() {
            assertParse("0.1E5", 0.1E5);
            assertParse("0.12E5", 0.12E5);
            assertParse("13.24E5", 13.24E5);
        }

        @Test
        void dot_should_between_number() {
            assertParse("1.", null);
            assertParse("-.5", null);
            assertParse(".5", null);
            assertParse("1.n", null);
            assertParse(".", null);
            assertParse("0.", null);
            assertParse(".0", null);
            assertParse("0.y", null);
            assertParse("0.f", null);
            assertParse("0.1", 0.1d);
            assertParse("0.9", 0.9d);
            assertParse("1.0", 1.0d);
            assertParse("9.1", 9.1d);
        }

        @Test
        void power_char_should_between_number() {
            assertParse(".E0", null);
            assertParse(".e0", null);
            assertParse("0E", null);
            assertParse("0e", null);
            assertParse(".e", null);
            assertParse("0ex", null);
            assertParse("0E.0", null);
            assertParse("0Ed", null);
        }

        @Nested
        class FromInteger {

            @Test
            void dot_in_integer() {
                assertParse("-0.0", -0.0d);
                assertParse("1.5", 1.5d);
                assertParse("10.05", 10.05d);
                assertParse("1__0.0__5", 1__0.0__5d);
                assertParse("0.123456789", 0.123456789d);
            }

            @Test
            void invalid_double() {
                assertParse("0x1.5", null);
                assertParse("1.1_", null);
            }

            @Test
            void power_number_in_integer() {
                assertParse("-0E5", -0E5);
                assertParse("10E05", 10E5);
                assertParse("10E1_5", 10E1_5);
                assertParse("10E15", 10E15);
                assertParse("10E-5", 10E-5);
                assertParse("10E+5", 10E5);
                assertParse("0E5", 0E5);
            }

            @Test
            void invalid_power_number() {
                assertParse("10E0.5", null);
                assertParse("10E5_", null);
                assertParse("10E_5", null);
                assertParse("10E0xF", null);
                assertParse("10Ea", null);
                assertParse("e1", null);
                assertParse("-e1", null);
                assertParse("0x1E1", 0x1E1);
                assertParse("10E", null);
                assertParse("10E+", null);
                assertParse("10E-", null);
            }
        }

        @Nested
        class FromLong {

            @Test
            void dot_in_long() {
                assertParse("2147483648.5", 2147483648.5d);
                assertParse("2147483648.05", 2147483648.05d);
                assertParse("2147483648.0__5", 2147483648.0__5d);
            }

            @Test
            void dot_should_between_number() {
                assertParse("2147483648.", null);
                assertParse("2147483648.n", null);
            }

            @Test
            void invalid_double() {
                assertParse("0x2147483648.5", null);
                assertParse("2147483648.1_", null);
            }

            @Test
            void power_number_in_integer() {
                assertParse("2147483648E05", 2147483648E5);
                assertParse("2147483648E1_5", 2147483648E1_5);
                assertParse("2147483648E15", 2147483648E15);
                assertParse("2147483648E-5", 2147483648E-5);
                assertParse("2147483648E+5", 2147483648E5);

            }

            @Test
            void invalid_power_number() {
                assertParse("2147483648E0.5", null);
                assertParse("2147483648E5_", null);
                assertParse("2147483648E0xF", null);
                assertParse("2147483648EA", null);
                assertParse("0x8FFFFFFFE1", 0x8FFFFFFFE1L);
                assertParse("2147483648E", null);
            }
        }

        @Nested
        class FromBigInteger {

            @Test
            void dot_in_big_integer() {
                assertParse("100000000000000000000.5", 100000000000000000000.5d);
                assertParse("100000000000000000000.05", 100000000000000000000.05d);
                assertParse("100000000000000000000.0__5", 100000000000000000000.0__5d);
            }

            @Test
            void dot_should_between_number() {
                assertParse("100000000000000000000.", null);
                assertParse("100000000000000000015.n", null);
            }

            @Test
            void invalid_double() {
                assertParse("0x100000000000000000015.5", null);
                assertParse("100000000000000000015.1_", null);
            }

            @Test
            void power_number_in_integer() {
                assertParse("100000000000000000015E05", 100000000000000000015E5);
                assertParse("100000000000000000015E1_5", 100000000000000000015E1_5);
                assertParse("100000000000000000015E15", 100000000000000000015E15);
                assertParse("100000000000000000015E-5", 100000000000000000015E-5);
                assertParse("100000000000000000015E+5", 100000000000000000015E5);

            }

            @Test
            void invalid_power_number() {
                assertParse("100000000000000000015E0.5", null);
                assertParse("100000000000000000015E5_", null);
                assertParse("100000000000000000015E0xF", null);
                assertParse("100000000000000000015EA", null);
                assertParse("0x100000000000000000015EA", new BigInteger("100000000000000000015EA", 16));
                assertParse("100000000000000000015E", null);
            }
        }
    }

    @Nested
    class ParseBigDecimal {

        @Test
        void to_big_decimal_with_huge_power() {
            assertParse("100E400", new BigDecimal("100E400"));
            assertParse("-100E400", new BigDecimal("-100E400"));
        }

        @Test
        void long_float_to_big_decimal() {
            String _400_0 = String.join("", Collections.nCopies(400, "0"));
            assertParse("1" + _400_0 + ".0", new BigDecimal("1" + _400_0 + ".0"));
            assertParse("-1" + _400_0 + ".0", new BigDecimal("-1" + _400_0 + ".0"));
        }

        @Test
        void big_decimal_with_precision() {
            assertParse("1.00BD", new BigDecimal("1.00"));
        }
    }

    @Nested
    class WithPostfix {

        @Test
        void invalid_postfix_number() {
            assertParse("1_d", null);
            assertParse("1_y", null);
            assertParse("1_s", null);
            assertParse("1_L", null);
            assertParse("1_f", null);
            assertParse("1_bd", null);
            assertParse("1_bi", null);
        }

        @Nested
        class IntegerParse {

            @Test
            void as_byte() {
                assertParse("0y", (byte) 0);
                assertParse("1y", (byte) 1);
                assertParse("-1y", (byte) -1);
                assertParse("-128y", (byte) -128);
                assertParse("127y", (byte) 127);
                assertParse("-0x80y", (byte) -128);
                assertParse("0x7fy", (byte) 127);

                assertParse("0Y", (byte) 0);
                assertParse("1Y", (byte) 1);

                assertParseOverflow("128y");
                assertParseOverflow("-129y");
            }

            @Test
            void as_short() {
                assertParse("0s", (short) 0);
                assertParse("1s", (short) 1);
                assertParse("-1s", (short) -1);
                assertParse("-0x8000s", (short) -32768);
                assertParse("0x7fffs", (short) 32767);

                assertParseOverflow("32768s");
                assertParseOverflow("-32769s");
            }

            @Test
            void as_int() {
                assertParse("0i", 0);
                assertParse("1I", 1);
                assertParse("-1I", -1);

                assertParseOverflow("2147483648i");
                assertParseOverflow("-2147483649i");
            }

            @Test
            void as_long() {
                assertParse("0l", 0L);
                assertParse("1l", 1L);
                assertParse("-1l", -1L);
                assertParse("-0x8000_0000l", -2147483648L);
                assertParse("0x7fff_ffffl", 2147483647L);
            }

            @Test
            void as_big_integer() {
                assertParse("0bi", BigInteger.valueOf(0));
                assertParse("10bi", BigInteger.valueOf(10));
                assertParse("-10bi", BigInteger.valueOf(-10));
            }

            @Test
            void as_big_decimal() {
                assertParse("0bd", BigDecimal.valueOf(0));
                assertParse("1bd", BigDecimal.valueOf(1));
                assertParse("-1bd", BigDecimal.valueOf(-1));
            }

            @Test
            void as_float() {
                assertParse("0f", 0.0f);
                assertParse("1f", 1.0f);
                assertParse("-1f", -1.0f);
            }

            @Test
            void as_double() {
                assertParse("0d", 0.0);
                assertParse("1d", 1.0);
                assertParse("-1d", -1.0);
            }
        }

        @Nested
        class LongParse {

            @Test
            void as_byte() {
                assertParseOverflow("0xffff_ffff_ffy");
                assertParseOverflow("-0xffff_ffff_ffy");
            }

            @Test
            void as_short() {
                assertParseOverflow("0xffff_ffff_ffs");
                assertParseOverflow("-0xffff_ffff_ffs");
            }

            @Test
            void as_long() {
                assertParse("-0x8000_0001l", -2147483649L);
                assertParse("0x8000_0000l", 2147483648L);

                assertParse("-0x8000_0000_0000_0000l", 0x8000_0000_0000_0000L);
                assertParse("0x7fff_ffff_ffff_ffff", 0x7fff_ffff_ffff_ffffL);
            }

            @Test
            void as_big_integer() {
                assertParse("0xffff_ffff_ffbi", new BigInteger("ffffffffff", 16));
                assertParse("-0xffff_ffff_ffbi", new BigInteger("-ffffffffff", 16));
            }

            @Test
            void as_big_decimal() {
                assertParse("2147483648bd", BigDecimal.valueOf(2147483648L));
                assertParse("-2147483649bd", BigDecimal.valueOf(-2147483649L));
            }

            @Test
            void as_float() {
                assertParse("2147483648f", 2147483648f);
                assertParse("-2147483648f", -2147483648f);
            }

            @Test
            void as_double() {
                assertParse("2147483648d", 2147483648d);
                assertParse("-2147483648d", -2147483648d);
            }
        }

        @Nested
        class BigIntegerParser {

            @Test
            void as_byte() {
                assertParseOverflow("0x8000_0000_0000_0000y");
                assertParseOverflow("-0x8000_0000_0000_0001y");
            }

            @Test
            void as_short() {
                assertParseOverflow("0x8000_0000_0000_0000s");
                assertParseOverflow("-0x8000_0000_0000_0001s");
            }

            @Test
            void as_long() {
//                assertParseOverflow("0x8000_0000_0000_0000l");
//                assertParseOverflow("-0x8000_0000_0000_0001l");
                assertParseOverflow("9223372036854775808L");
                assertParseOverflow("-9223372036854775809L");
            }

            @Test
            void as_big_integer() {
                assertParse("0x8000_0000_0000_0000bi", new BigInteger("8000000000000000", 16));
                assertParse("-0x8000_0000_0000_0001bi", new BigInteger("-8000000000000001", 16));
            }

            @Test
            void as_big_decimal() {
                assertParse("9223372036854775808bd", new BigDecimal("9223372036854775808"));
                assertParse("-9223372036854775809bd", new BigDecimal("-9223372036854775809"));
            }

            @Test
            void as_float() {
                assertParse("9223372036854775808f", 9223372036854775808f);
                assertParse("-9223372036854775808f", -9223372036854775808f);
            }

            @Test
            void as_double() {
                assertParse("9223372036854775808d", 9223372036854775808d);
                assertParse("-9223372036854775808d", -9223372036854775808d);
            }
        }

        @Nested
        class DotFloatParser {

            @Test
            void as_byte() {
                assertParseOverflow("0.0y");
                assertParseOverflow("2147483648.0y");
                assertParseOverflow("9223372036854775808.0y");
            }

            @Test
            void as_short() {
                assertParseOverflow("0.0s");
                assertParseOverflow("2147483648.0s");
                assertParseOverflow("9223372036854775808.0s");
            }

            @Test
            void as_long() {
                assertParseOverflow("0.0l");
                assertParseOverflow("2147483648.0l");
                assertParseOverflow("9223372036854775808.0l");
            }

            @Test
            void as_big_integer() {
                assertParseOverflow("0.0bi");
                assertParseOverflow("2147483648.0bi");
                assertParseOverflow("9223372036854775808.0bi");
            }

            @Test
            void as_big_decimal() {
                assertParse("0.0bd", new BigDecimal("0.0"));
                assertParse("2147483648.0bd", new BigDecimal("2147483648.0"));
                assertParse("9223372036854775808.0bd", new BigDecimal("9223372036854775808.0"));
            }

            @Test
            void as_float() {
                assertParse("0.0f", 0.0f);
                assertParse("2147483648.0f", 2147483648.0f);
                assertParse("9223372036854775808.0f", 9223372036854775808.0f);
            }

            @Test
            void as_double() {
                assertParse("0.0d", 0.0d);
                assertParse("2147483648.0d", 2147483648.0d);
                assertParse("9223372036854775808.0d", 9223372036854775808.0d);
            }
        }

        @Nested
        class PowerFloatParser {

            @Test
            void as_byte() {
                assertParseOverflow("0e0y");
                assertParseOverflow("2147483648e0y");
                assertParseOverflow("9223372036854775808e0y");
            }

            @Test
            void as_short() {
                assertParseOverflow("0e0s");
                assertParseOverflow("2147483648e0s");
                assertParseOverflow("9223372036854775808e0s");
            }

            @Test
            void as_long() {
                assertParseOverflow("0e0l");
                assertParseOverflow("2147483648e0l");
                assertParseOverflow("9223372036854775808e0l");
            }

            @Test
            void as_big_integer() {
                assertParseOverflow("0e0bi");
                assertParseOverflow("2147483648e0bi");
                assertParseOverflow("9223372036854775808e0bi");
            }

            @Test
            void as_big_decimal() {
                assertParse("0e0bd", new BigDecimal("0e0"));
                assertParse("2147483648e0bd", new BigDecimal("2147483648e0"));
                assertParse("9223372036854775808e0bd", new BigDecimal("9223372036854775808e0"));
            }

            @Test
            void as_float() {
                assertParse("0e0f", 0e0f);
                assertParse("2147483648e0f", 2147483648e0f);
                assertParse("9223372036854775808e0f", 9223372036854775808e0f);
            }

            @Test
            void as_double() {
                assertParse("0e0d", 0e0d);
                assertParse("2147483648e0d", 2147483648e0d);
                assertParse("9223372036854775808e0d", 9223372036854775808e0d);
            }

            @Test
            void overflow() {
                assertParseOverflow("1E200f");
                assertParseOverflow("1E400d");
            }
        }

        @Nested
        class UnsignedForNonDecRadix {

            @Nested
            class IntegerParse {

                @Test
                void as_byte() {
                    assertParse("0xffy", (byte) -1);
                    assertParse("0xffs", (short) 255);
                    assertParseOverflow("0x100y");
                    assertParseOverflow("-0x81y");

                    assertParse("0377y", (byte) -1);
                    assertParseOverflow("0400y");
                    assertParseOverflow("-0201y");

                    assertParse("0b11111111y", (byte) -1);
                    assertParseOverflow("0b100000000y");
                    assertParseOverflow("-0b100000001y");
                }

                @Test
                void as_short() {
                    assertParse("0xffffs", (short) -1);
                    assertParse("0xffffi", 65535);
                    assertParseOverflow("0x10000s");
                    assertParseOverflow("-0x8001s");

                    assertParse("0177777s", (short) -1);
                    assertParseOverflow("0200000s");
                    assertParseOverflow("-0200001s");

                    assertParse("0b1111111111111111s", (short) -1);
                    assertParseOverflow("0b10000000000000000s");
                    assertParseOverflow("-0b10000000000000001s");
                }

                @Test
                void as_integer() {
                    assertParse("0xffffffffi", -1);
                    assertParse("0xffffffffl", 0xffffffffL);
                    assertParseOverflow("0x100000000i");
                    assertParseOverflow("-0x80000001i");

                    assertParse("037777777777i", -1);
                    assertParseOverflow("040000000000i");
                    assertParseOverflow("-020000000001s");

                    assertParse("0b11111111111111111111111111111111i", -1);
                    assertParseOverflow("0b100000000000000000000000000000000i");
                    assertParseOverflow("-0b100000000000000000000000000000001i");
                }

                @Test
                void as_long() {
                    assertParse("0xffffffffffffffffl", -1L);
                    assertParse("0xffffffffffffffffbi", new BigInteger("18446744073709551615"));
                    assertParseOverflow("0x10000000000000000l");
                    assertParseOverflow("-0x8000000000000001l");

                    assertParse("01777777777777777777777L", -1L);
                    assertParse("01000000000000000000000L", Long.MIN_VALUE);
                    assertParseOverflow("02000000000000000000000L");
                    assertParseOverflow("-01000000000000000000001L");

                    assertParse("0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_11111111L", -1L);
                    assertParse("0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L", Long.MIN_VALUE);
                    assertParseOverflow("0b1_00000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000L");
                    assertParseOverflow("-0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000001L");
                }
            }
        }
    }

    @Nested
    class WithoutPostfix {

        @Nested
        class IntegerParse {

            @Nested
            class Radix10 {

                @Nested
                class ToJavaInteger {

                    @Test
                    void single_number_char() {
                        assertParse("0", 0);
                        assertParse("1", 1);
                        assertParse("2", 2);
                        assertParse("3", 3);
                        assertParse("4", 4);
                        assertParse("5", 5);
                        assertParse("6", 6);
                        assertParse("7", 7);
                        assertParse("8", 8);
                        assertParse("9", 9);
                    }

                    @Test
                    void supported_number_range() {
                        assertParse("2147483647", Integer.MAX_VALUE);
                        assertParse("-2147483648", Integer.MIN_VALUE);
                    }

                    @Test
                    void positive() {
                        assertParse("+24", 24);
                    }

                    @Test
                    void invalid_number() {
                        assertParse(null, null);
                        assertParse("+", null);
                        assertParse("-", null);
                        assertParse("1_", null);
                        assertParse("1x", null);
                        assertParse("F", null);
                        assertParse("e", null);
                        assertParse("y", null);
                        assertParse("s", null);
                        assertParse("l", null);
                        assertParse("bi", null);
                        assertParse("bd", null);
                        assertParse("d", null);
                        assertParse("-F", null);
                        assertParse("-y", null);
                        assertParse("-s", null);
                        assertParse("-l", null);
                        assertParse("-bi", null);
                        assertParse("-bd", null);
                        assertParse("-d", null);
                    }
                }

                @Nested
                class ToJavaLong {
                    @Test
                    void supported_number_range() {
                        assertParse("9223372036854775807", Long.MAX_VALUE);
                        assertParse("2147483648", Integer.MAX_VALUE + 1L);
                        assertParse("-2147483649", Integer.MIN_VALUE - 1L);
                        assertParse("-9223372036854775808", Long.MIN_VALUE);
                    }

                    @Test
                    void positive() {
                        assertParse("+9223372036854775807", 9223372036854775807L);
                    }

                    @Test
                    void invalid_number() {
                        assertParse("100000000005_", null);
                        assertParse("100000000005xx", null);
                    }
                }

                @Nested
                class ToJavaBigInteger {

                    @Test
                    void supported_number_range() {
                        assertParse("9223372036854775808", BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(1)));
                        assertParse("-9223372036854775809", BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.valueOf(1)));
                    }

                    @Test
                    void positive() {
                        assertParse("+10000000000000000005", new BigInteger("+10000000000000000005"));
                    }

                    @Test
                    void invalid_number() {
                        assertParse("10000000000000000005_", null);
                        assertParse("10000000000000000005xx", null);
                    }
                }

            }

            @Nested
            class Radix16 {

                @Nested
                class ToJavaInteger {

                    @Test
                    void single_number_char() {
                        assertParse("0x0", 0);
                        assertParse("0x1", 1);
                        assertParse("0x2", 2);
                        assertParse("0x3", 3);
                        assertParse("0x4", 4);
                        assertParse("0X5", 5);
                        assertParse("0X6", 6);
                        assertParse("0X7", 7);
                        assertParse("0X8", 8);
                        assertParse("0X9", 9);
                        assertParse("0Xa", 0xa);
                        assertParse("0xb", 0xb);
                        assertParse("0xc", 0xc);
                        assertParse("0xd", 0xd);
                        assertParse("0xe", 0xe);
                        assertParse("0xf", 0xf);
                        assertParse("0xA", 0xa);
                        assertParse("0xB", 0xb);
                        assertParse("0xC", 0xc);
                        assertParse("0xD", 0xd);
                        assertParse("0xE", 0xe);
                        assertParse("0xF", 0xf);
                    }

                    @Test
                    void supported_number_range() {
                        assertParse("0x7fffffff", Integer.MAX_VALUE);
                        assertParse("-0x80000000", Integer.MIN_VALUE);
                    }

                    @Test
                    void supported_unsigned_number_range() {
                        assertParse("0xffffffff", -1);
                        assertParse("0x80000000", Integer.MIN_VALUE);
                    }

                    @Test
                    void positive() {
                        assertParse("+0xff", 0xff);
                    }

                    @Test
                    void invalid_number() {
                        assertParse("0x", null);
                        assertParse("+0x", null);
                        assertParse("-0x", null);
                        assertParse("0x1_", null);
                        assertParse("0x1x", null);
                        assertParse("0xG", null);
                    }
                }

                @Nested
                class ToJavaLong {

                    @Test
                    void supported_number_range() {
                        assertParse("0x7FFFFFFFFFFFFFFF", Long.MAX_VALUE);
                        assertParse("0x100000000", 0x100000000L);

                        assertParse("-0x80000001", Integer.MIN_VALUE - 1L);
                        assertParse("-0x8000000000000000", Long.MIN_VALUE);
                    }

                    @Test
                    void supported_unsigned_number_range() {
                        assertParse("0xFFFFFFFFFFFFFFFF", -1L);
                        assertParse("0x8000000000000000", Long.MIN_VALUE);
                    }

                    @Test
                    void positive() {
                        assertParse("+0xfffffffffff", 0xfffffffffffL);
                    }

                    @Test
                    void invalid_number() {
                        assertParse("100000000005_", null);
                        assertParse("100000000005xx", null);
                    }
                }

                @Nested
                class ToJavaBigInteger {

                    @Test
                    void supported_number_range() {
                        assertParse("0x10000000000000000", new BigInteger("10000000000000000", 16));
                        assertParse("-0x8000000000000001", new BigInteger("-8000000000000001", 16));
                    }

                    @Test
                    void positive() {
                        assertParse("+0x20000000000000000", new BigInteger("20000000000000000", 16));
                    }

                    @Test
                    void invalid_number() {
                        assertParse("0x10000000000000000005_", null);
                        assertParse("0x10000000000000000005xx", null);
                    }
                }
            }

            @Nested
            class Radix2 {

                @Nested
                class ToJavaInteger {

                    @Test
                    void single_number_char() {
                        assertParse("0b0", 0);
                        assertParse("0B1", 1);
                    }

                    @Test
                    void supported_number_range() {
                        assertParse("0b0111_1111_1111_1111_1111_1111_1111_1111", Integer.MAX_VALUE);
                        assertParse("-0b1000_0000_0000_0000_0000_0000_0000_0000", Integer.MIN_VALUE);
                    }

                    @Test
                    void supported_unsigned_number_range() {
                        assertParse("0b1111_1111_1111_1111_1111_1111_1111_1111", -1);
                        assertParse("0b1000_0000_0000_0000_0000_0000_0000_0000", Integer.MIN_VALUE);
                    }

                    @Test
                    void positive() {
                        assertParse("+0b11", 3);
                    }

                    @Test
                    void invalid_number() {
                        assertParse("0b0111_1111_1111_1111_1111_1111_1111_", null);
                        assertParse("0b0111_1111_1111_1111_1111_1111_1111_x", null);
                    }
                }

                @Nested
                class ToJavaLong {

                    @Test
                    void supported_number_range() {
                        assertParse("0b01111111_11111111_11111111_11111111_11111111_11111111_11111111_11111111", Long.MAX_VALUE);
                        assertParse("0b1_00000000_00000000_00000000_00000000", 0x100000000L);
                        assertParse("-0b10000000_00000000_00000000_00000001", Integer.MIN_VALUE - 1L);
                        assertParse("-0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000", Long.MIN_VALUE);
                    }

                    @Test
                    void supported_unsigned_number_range() {
                        assertParse("0b11111111_11111111_11111111_11111111_11111111_11111111_11111111_11111111", -1L);
                        assertParse("0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000", Long.MIN_VALUE);
                    }

                    @Test
                    void positive() {
                        assertParse("+0b00001111_11111111_11111111_11111111_11111111_11111111", 0xfff_ffff_ffffL);
                    }

                    @Test
                    void invalid_number() {
                        assertParse("0b01111111_11111111_11111111_11111111_11111111_11111111_11111111_1111_", null);
                        assertParse("0b01111111_11111111_11111111_11111111_11111111_11111111_11111111_1111x", null);
                    }
                }

                @Nested
                class ToJavaBigInteger {

                    @Test
                    void supported_number_range() {
                        assertParse("0b1_00000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000", new BigInteger("10000000000000000", 16));
                        assertParse("-0b10000000_00000000_00000000_00000000_00000000_00000000_00000000_00000001", new BigInteger("-9223372036854775809"));
                    }

                    @Test
                    void positive() {
                        assertParse("+0b1_00000000_00000000_00000000_00000000_00000000_00000000_00000000_00000000", new BigInteger("10000000000000000", 16));
                    }

                    @Test
                    void invalid_number() {
                        assertParse("0b1_00000000_00000000_00000000_00000000_00000000_00000000_00000000_0000_", null);
                        assertParse("0b1_00000000_00000000_00000000_00000000_00000000_00000000_00000000_0000b", null);
                    }
                }
            }

            @Nested
            class Radix8 {

                @Nested
                class ToJavaInteger {

                    @Test
                    void single_number_char() {
                        assertParse("00", 0);
                        assertParse("01", 1);
                        assertParse("02", 2);
                        assertParse("03", 3);
                        assertParse("04", 4);
                        assertParse("05", 5);
                        assertParse("06", 6);
                        assertParse("07", 7);
                        assertParse("010", 8);
                    }

                    @Test
                    void supported_number_range() {
                        assertParse("017777777777", Integer.MAX_VALUE);
                        assertParse("-020000000000", Integer.MIN_VALUE);
                    }

                    @Test
                    void supported_unsigned_number_range() {
                        assertParse("037777777777", -1);
                        assertParse("020000000000", Integer.MIN_VALUE);
                    }

                    @Test
                    void positive() {
                        assertParse("+017777777777", Integer.MAX_VALUE);
                    }

                }

                @Nested
                class ToJavaLong {

                    @Test
                    void supported_number_range() {
                        assertParse("0777777777777777777777", Long.MAX_VALUE);
                        assertParse("040000000000", 0x100000000L);

                        assertParse("-020000000001", Integer.MIN_VALUE - 1L);
                        assertParse("-01000000000000000000000", Long.MIN_VALUE);
                    }

                    @Test
                    void supported_unsigned_number_range() {
                        assertParse("01777777777777777777777", -1L);
                        assertParse("01000000000000000000000", Long.MIN_VALUE);
                    }

                    @Test
                    void positive() {
                        assertParse("+0777777777777777777777", Long.MAX_VALUE);
                    }
                }

                @Nested
                class ToJavaBigInteger {

                    @Test
                    void supported_number_range() {
                        assertParse("02000000000000000000000", new BigInteger("10000000000000000", 16));
                        assertParse("-01000000000000000000001", new BigInteger("-8000000000000001", 16));
                    }

                    @Test
                    void positive() {
                        assertParse("+02000000000000000000000", new BigInteger("10000000000000000", 16));
                    }
                }
            }
        }
    }

    @Test
    void delimiter() {
        assertParse("1_000", 1_000);
        assertParse("1_000_000", 1_000_000);
        assertParse("1_000_000_000_000", 1_000_000_000_000L);
        assertParse("1_000_000_000_000_000_000_000_000", new BigInteger("1000000000000000000000000"));

        assertParse("0x1_000", 0x1_000);
        assertParse("0xfff_ffff_ffff", 0xfff_ffff_ffffL);
        assertParse("0x800000000000_000_012", new BigInteger("800000000000000012", 16));
    }
}
