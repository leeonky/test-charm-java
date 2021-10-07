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
    }
}