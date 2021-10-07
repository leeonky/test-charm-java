package com.github.leeonky.util;

import org.assertj.core.api.Java6Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class NumberUtilTest {

    @Nested
    class CalculationType {

        void same_number_type(List<List<Class<?>>> types) {
            types.forEach(type -> {
                assertThat(NumberUtil.calculationType(type.get(0), type.get(1))).isEqualTo(type.get(0));
                assertThat(NumberUtil.calculationType(type.get(1), type.get(0))).isEqualTo(type.get(0));
                assertThat(NumberUtil.calculationType(type.get(0), type.get(1))).isEqualTo(type.get(1));
                assertThat(NumberUtil.calculationType(type.get(1), type.get(0))).isEqualTo(type.get(1));
            });
        }

        void use_left_type(List<List<Class<?>>> types) {
            types.forEach(type -> {
                assertThat(NumberUtil.calculationType(type.get(0), type.get(1))).isEqualTo(type.get(0));
                assertThat(NumberUtil.calculationType(type.get(1), type.get(0))).isEqualTo(type.get(0));
            });
        }

        void should_use_big_big_decimal(List<List<Class<?>>> types) {
            types.forEach(type -> {
                assertThat(NumberUtil.calculationType(type.get(0), type.get(1))).isEqualTo(BigDecimal.class);
                assertThat(NumberUtil.calculationType(type.get(1), type.get(0))).isEqualTo(BigDecimal.class);
            });
        }

        @Test
        void matrix_of_number_type() {
            same_number_type(asList(
                    asList(Byte.class, Byte.class),
                    asList(Short.class, Short.class),
                    asList(Integer.class, Integer.class),
                    asList(Long.class, Long.class),
                    asList(Float.class, Float.class),
                    asList(Double.class, Double.class),
                    asList(BigInteger.class, BigInteger.class),
                    asList(BigDecimal.class, BigDecimal.class)
            ));

            use_left_type(asList(
                    asList(Short.class, Byte.class),
                    asList(Integer.class, Byte.class),
                    asList(Long.class, Byte.class),
                    asList(Float.class, Byte.class),
                    asList(Double.class, Byte.class),
                    asList(BigInteger.class, Byte.class),
                    asList(BigDecimal.class, Byte.class),

                    asList(Integer.class, Short.class),
                    asList(Long.class, Short.class),
                    asList(Float.class, Short.class),
                    asList(Double.class, Short.class),
                    asList(BigInteger.class, Short.class),
                    asList(BigDecimal.class, Short.class),
                    asList(Long.class, Integer.class),
                    asList(Float.class, Integer.class),
                    asList(Double.class, Integer.class),
                    asList(BigInteger.class, Integer.class),
                    asList(BigDecimal.class, Integer.class),
                    asList(Float.class, Long.class),
                    asList(Double.class, Long.class),
                    asList(BigInteger.class, Long.class),
                    asList(BigDecimal.class, Long.class),
                    asList(Double.class, Float.class),
                    asList(BigDecimal.class, Float.class),
                    asList(BigDecimal.class, Double.class),
                    asList(BigDecimal.class, BigInteger.class)
            ));

            should_use_big_big_decimal(asList(
                    asList(BigInteger.class, Float.class),
                    asList(BigInteger.class, Double.class)
            ));
        }

        @Test
        void should_box_first() {
            assertThat(NumberUtil.calculationType(int.class, long.class)).isEqualTo(Long.class);
        }
    }

    @Nested
    class Box {

        @Test
        void box_class() {
            Java6Assertions.assertThat(NumberUtil.boxedClass(char.class)).isEqualTo(Character.class);
            Java6Assertions.assertThat(NumberUtil.boxedClass(int.class)).isEqualTo(Integer.class);
            Java6Assertions.assertThat(NumberUtil.boxedClass(short.class)).isEqualTo(Short.class);
            Java6Assertions.assertThat(NumberUtil.boxedClass(long.class)).isEqualTo(Long.class);
            Java6Assertions.assertThat(NumberUtil.boxedClass(float.class)).isEqualTo(Float.class);
            Java6Assertions.assertThat(NumberUtil.boxedClass(double.class)).isEqualTo(Double.class);
            Java6Assertions.assertThat(NumberUtil.boxedClass(boolean.class)).isEqualTo(Boolean.class);
        }
    }

    @Nested
    class Calculate {

        @Nested
        class Plus {

            void assertPlus(Number left, Number right, Number result) {
                assertThat(NumberUtil.plus(left, right)).isEqualTo(result);
            }

            @Test
            void same_type() {
                assertPlus((byte) 1, (byte) 1, 2);
                assertPlus((short) 1, (short) 1, 2);
                assertPlus(1, 1, 2);
                assertPlus(1L, 1L, 2L);
                assertPlus(1f, 1f, 2f);
                assertPlus(1d, 1d, 2d);
                assertPlus(BigInteger.valueOf(1), BigInteger.valueOf(1), BigInteger.valueOf(2));
                assertPlus(BigDecimal.valueOf(1), BigDecimal.valueOf(1), BigDecimal.valueOf(2));
            }

            @Test
            void different_type() {
                assertPlus(1, 1L, 2L);
                assertPlus(1.0, BigDecimal.valueOf(1), BigDecimal.valueOf(2.0));
            }
        }

        @Nested
        class Sub {

            void assertSub(Number left, Number right, Number result) {
                assertThat(NumberUtil.subtract(left, right)).isEqualTo(result);
            }

            @Test
            void same_type() {
                assertSub((byte) 3, (byte) 1, 2);
                assertSub((short) 3, (short) 1, 2);
                assertSub(3, 1, 2);
                assertSub(3L, 1L, 2L);
                assertSub(3f, 1f, 2f);
                assertSub(3d, 1d, 2d);
                assertSub(BigInteger.valueOf(3), BigInteger.valueOf(1), BigInteger.valueOf(2));
                assertSub(BigDecimal.valueOf(3), BigDecimal.valueOf(1), BigDecimal.valueOf(2));
            }

            @Test
            void different_type() {
                assertSub(3, 1L, 2L);
                assertSub(3.0, BigDecimal.valueOf(1), BigDecimal.valueOf(2.0));
            }
        }

        @Nested
        class Div {

            void assertDiv(Number left, Number right, Number result) {
                assertThat(NumberUtil.divide(left, right)).isEqualTo(result);
            }

            @Test
            void same_type() {
                assertDiv((byte) 2, (byte) 1, 2);
                assertDiv((short) 2, (short) 1, 2);
                assertDiv(2, 1, 2);
                assertDiv(2L, 1L, 2L);
                assertDiv(2f, 1f, 2f);
                assertDiv(2d, 1d, 2d);
                assertDiv(BigInteger.valueOf(2), BigInteger.valueOf(1), BigInteger.valueOf(2));
                assertDiv(BigDecimal.valueOf(2), BigDecimal.valueOf(1), BigDecimal.valueOf(2));
            }

            @Test
            void different_type() {
                assertDiv(2, 1L, 2L);
                assertDiv(2.0, BigDecimal.valueOf(1), BigDecimal.valueOf(2.0));
            }
        }

        @Nested
        class Mul {

            void assertMul(Number left, Number right, Number result) {
                assertThat(NumberUtil.multiply(left, right)).isEqualTo(result);
            }

            @Test
            void same_type() {
                assertMul((byte) 2, (byte) 1, 2);
                assertMul((short) 2, (short) 1, 2);
                assertMul(2, 1, 2);
                assertMul(2L, 1L, 2L);
                assertMul(2f, 1f, 2f);
                assertMul(2d, 1d, 2d);
                assertMul(BigInteger.valueOf(2), BigInteger.valueOf(1), BigInteger.valueOf(2));
                assertMul(BigDecimal.valueOf(2), BigDecimal.valueOf(1), BigDecimal.valueOf(2));
            }

            @Test
            void different_type() {
                assertMul(2, 1L, 2L);
                assertMul(2.0, BigDecimal.valueOf(1), BigDecimal.valueOf(2.0));
            }
        }

        @Nested
        class Compare {
            void assertEqual(Number left, Number right, int result) {
                assertThat(NumberUtil.compare(left, right)).isEqualTo(result);
            }

            @Test
            void same_type() {
                assertEqual((byte) 2, (byte) 1, Byte.compare((byte) 2, (byte) 1));
                assertEqual((short) 2, (short) 1, Short.compare((short) 2, (short) 1));
                assertEqual(2, 1, Integer.compare(2, 1));
                assertEqual(2L, 1L, Long.compare(2, 1));
                assertEqual(2f, 1f, Float.compare(2f, 1f));
                assertEqual(2d, 1d, Double.compare(2d, 1d));
                assertEqual(BigInteger.valueOf(2), BigInteger.valueOf(1),
                        BigInteger.valueOf(2).compareTo(BigInteger.valueOf(1)));
                assertEqual(BigDecimal.valueOf(2), BigDecimal.valueOf(1),
                        BigDecimal.valueOf(2).compareTo(BigDecimal.valueOf(1)));
            }

            @Test
            void different_type() {
                assertEqual(2, 1L, Long.compare(2, 1L));
                assertEqual(2.0, BigDecimal.valueOf(1), BigDecimal.valueOf(2.0).compareTo(BigDecimal.valueOf(1)));
            }
        }
    }
}