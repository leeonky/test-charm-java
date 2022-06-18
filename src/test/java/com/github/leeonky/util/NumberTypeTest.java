package com.github.leeonky.util;

import org.assertj.core.api.Java6Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumberTypeTest {

    @Nested
    class CalculationType {

        void same_number_type(List<List<Class<?>>> types) {
            types.forEach(type -> {
                assertThat(NumberType.calculationType(type.get(0), type.get(1))).isEqualTo(type.get(0));
                assertThat(NumberType.calculationType(type.get(1), type.get(0))).isEqualTo(type.get(0));
                assertThat(NumberType.calculationType(type.get(0), type.get(1))).isEqualTo(type.get(1));
                assertThat(NumberType.calculationType(type.get(1), type.get(0))).isEqualTo(type.get(1));
            });
        }

        void use_left_type(List<List<Class<?>>> types) {
            types.forEach(type -> {
                assertThat(NumberType.calculationType(type.get(0), type.get(1))).isEqualTo(type.get(0));
                assertThat(NumberType.calculationType(type.get(1), type.get(0))).isEqualTo(type.get(0));
            });
        }

        void should_use_big_big_decimal(List<List<Class<?>>> types) {
            types.forEach(type -> {
                assertThat(NumberType.calculationType(type.get(0), type.get(1))).isEqualTo(BigDecimal.class);
                assertThat(NumberType.calculationType(type.get(1), type.get(0))).isEqualTo(BigDecimal.class);
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
            assertThat(NumberType.calculationType(int.class, long.class)).isEqualTo(Long.class);
        }
    }

    @Nested
    class Box {

        @Test
        void box_class() {
            Java6Assertions.assertThat(BeanClass.boxedClass(char.class)).isEqualTo(Character.class);
            Java6Assertions.assertThat(BeanClass.boxedClass(int.class)).isEqualTo(Integer.class);
            Java6Assertions.assertThat(BeanClass.boxedClass(short.class)).isEqualTo(Short.class);
            Java6Assertions.assertThat(BeanClass.boxedClass(long.class)).isEqualTo(Long.class);
            Java6Assertions.assertThat(BeanClass.boxedClass(float.class)).isEqualTo(Float.class);
            Java6Assertions.assertThat(BeanClass.boxedClass(double.class)).isEqualTo(Double.class);
            Java6Assertions.assertThat(BeanClass.boxedClass(boolean.class)).isEqualTo(Boolean.class);
        }
    }

    @Nested
    class Calculate {

        @Nested
        class Plus {

            void assertPlus(Number left, Number right, Number result) {
                assertThat(new NumberType().plus(left, right)).isEqualTo(result);
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
                assertThat(new NumberType().subtract(left, right)).isEqualTo(result);
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
                assertThat(new NumberType().divide(left, right)).isEqualTo(result);
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
                assertThat(new NumberType().multiply(left, right)).isEqualTo(result);
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
                assertCompare(new NumberType(), left, right, result);
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

            @Test
            void compare_double_with_epsilon() {
                NumberType numberType = new NumberType();
                assertCompare(numberType, 1.0, 1.0, 0);
                assertCompare(numberType, 1.0, 1.0 + numberType.getDoubleEpsilon() / 10, 0);
                assertCompare(numberType, 1.0, 1.0 - numberType.getDoubleEpsilon() / 10, 0);
                assertCompare(numberType, 1.0, 1.0 + numberType.getDoubleEpsilon() + numberType.getDoubleEpsilon(), -1);
                assertCompare(numberType, 1.0, 1.0 - numberType.getDoubleEpsilon() - numberType.getDoubleEpsilon(), 1);
            }

            @Test
            void compare_float_with_epsilon() {
                NumberType numberType = new NumberType();
                assertCompare(numberType, 1.0f, 1.0f, 0);
                assertCompare(numberType, 1.0f, 1.0f + numberType.getFloatEpsilon() / 10, 0);
                assertCompare(numberType, 1.0f, 1.0f - numberType.getFloatEpsilon() / 10, 0);
                assertCompare(numberType, 1.0f, 1.0f + numberType.getFloatEpsilon() + numberType.getFloatEpsilon(), -1);
                assertCompare(numberType, 1.0f, 1.0f - numberType.getFloatEpsilon() - numberType.getFloatEpsilon(), 1);
            }

            private void assertCompare(NumberType numberType, Number left, Number right, int expected) {
                assertThat(numberType.compare(left, right)).isEqualTo(expected);
            }
        }

        @Nested
        class Negate {
            void assertNegate(Number left, Number result) {
                assertThat(new NumberType().negate(left)).isEqualTo(result);
            }

            @Test
            void same_type() {
                assertNegate((byte) 1, (byte) -1);
                assertNegate((short) 1, (short) -1);
                assertNegate(1, -1);
                assertNegate(1L, -1L);
                assertNegate(1f, -1f);
                assertNegate(1d, -1d);
                assertNegate(BigInteger.valueOf(1), BigInteger.valueOf(-1));
                assertNegate(BigDecimal.valueOf(1), BigDecimal.valueOf(-1));
            }

        }
    }

    @Nested
    class ConvertNumber {
        NumberType numberType = new NumberType();

        @Test
        void raise_error_when_unexpected_number_type() {
            assertThatThrownBy(() -> numberType.convert(1, UnexpectedNumber.class))
                    .hasMessageContaining("Cannot convert 1 to com.github.leeonky.util.NumberTypeTest$UnexpectedNumber");
        }

        @Nested
        class ConvertToByte {

            @Test
            void convert_to_byte_with_out_error() {
                assertThat(numberType.convert((byte) 1, byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Byte.valueOf((byte) 1), byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert((short) 1, byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Short.valueOf("1"), byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(1, byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Integer.valueOf("1"), byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(1L, byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Long.valueOf("1"), byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(1.0F, byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Float.valueOf("1.0"), byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(1.0D, byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Double.valueOf("1.0"), byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(BigInteger.valueOf(1), byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(BigDecimal.valueOf(1), byte.class)).isEqualTo((byte) 1);
            }

            @Test
            void should_raise_error_when_value_over_flow() {
                assertThatThrownBy(() -> numberType.convert((short) 128, byte.class))
                        .hasMessageContaining("Cannot convert 128 to byte");

                assertThatThrownBy(() -> numberType.convert((short) -129, byte.class))
                        .hasMessageContaining("Cannot convert -129 to byte");

                assertThatThrownBy(() -> numberType.convert(128, byte.class))
                        .hasMessageContaining("Cannot convert 128 to byte");

                assertThatThrownBy(() -> numberType.convert(-129, byte.class))
                        .hasMessageContaining("Cannot convert -129 to byte");

                assertThatThrownBy(() -> numberType.convert(128L, byte.class))
                        .hasMessageContaining("Cannot convert 128 to byte");

                assertThatThrownBy(() -> numberType.convert(-129L, byte.class))
                        .hasMessageContaining("Cannot convert -129 to byte");

                assertThatThrownBy(() -> numberType.convert(128F, byte.class))
                        .hasMessageContaining("Cannot convert 128.0 to byte");

                assertThatThrownBy(() -> numberType.convert(-129F, byte.class))
                        .hasMessageContaining("Cannot convert -129.0 to byte");

                assertThatThrownBy(() -> numberType.convert(128D, byte.class))
                        .hasMessageContaining("Cannot convert 128.0 to byte");

                assertThatThrownBy(() -> numberType.convert(-129D, byte.class))
                        .hasMessageContaining("Cannot convert -129.0 to byte");

                assertThatThrownBy(() -> numberType.convert(BigInteger.valueOf(128), byte.class))
                        .hasMessageContaining("Cannot convert 128 to byte");

                assertThatThrownBy(() -> numberType.convert(BigInteger.valueOf(-129), byte.class))
                        .hasMessageContaining("Cannot convert -129 to byte");

                assertThatThrownBy(() -> numberType.convert(BigDecimal.valueOf(128), byte.class))
                        .hasMessageContaining("Cannot convert 128 to byte");

                assertThatThrownBy(() -> numberType.convert(BigDecimal.valueOf(-129), byte.class))
                        .hasMessageContaining("Cannot convert -129 to byte");
            }

            @Test
            void should_raise_error_when_value_has_decimal() {
                assertThatThrownBy(() -> numberType.convert(1.1F, byte.class))
                        .hasMessageContaining("Cannot convert 1.1 to byte");

                assertThatThrownBy(() -> numberType.convert(1.1D, byte.class))
                        .hasMessageContaining("Cannot convert 1.1 to byte");

                assertThatThrownBy(() -> numberType.convert(new BigDecimal("1.1"), byte.class))
                        .hasMessageContaining("Cannot convert 1.1 to byte");
            }
        }

        @Nested
        class ConvertToBoxedByte {

            @Test
            void convert_to_byte_with_out_error() {
                assertThat(numberType.convert((byte) 1, Byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Byte.valueOf((byte) 1), Byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert((short) 1, Byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Short.valueOf("1"), Byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(1, Byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Integer.valueOf("1"), Byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(1L, Byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Long.valueOf("1"), Byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(1.0F, Byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Float.valueOf("1.0"), Byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(1.0D, Byte.class)).isEqualTo((byte) 1);
                assertThat(numberType.convert(Double.valueOf("1.0"), Byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(BigInteger.valueOf(1), Byte.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(BigDecimal.valueOf(1), Byte.class)).isEqualTo((byte) 1);
            }

            @Test
            void should_raise_error_when_value_over_flow() {
                assertThatThrownBy(() -> numberType.convert((short) 128, Byte.class))
                        .hasMessageContaining("Cannot convert 128 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert((short) -129, Byte.class))
                        .hasMessageContaining("Cannot convert -129 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(128, Byte.class))
                        .hasMessageContaining("Cannot convert 128 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(-129, Byte.class))
                        .hasMessageContaining("Cannot convert -129 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(128L, Byte.class))
                        .hasMessageContaining("Cannot convert 128 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(-129L, Byte.class))
                        .hasMessageContaining("Cannot convert -129 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(128F, Byte.class))
                        .hasMessageContaining("Cannot convert 128.0 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(-129F, Byte.class))
                        .hasMessageContaining("Cannot convert -129.0 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(128D, Byte.class))
                        .hasMessageContaining("Cannot convert 128.0 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(-129D, Byte.class))
                        .hasMessageContaining("Cannot convert -129.0 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(BigInteger.valueOf(128), Byte.class))
                        .hasMessageContaining("Cannot convert 128 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(BigInteger.valueOf(-129), Byte.class))
                        .hasMessageContaining("Cannot convert -129 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(BigDecimal.valueOf(128), Byte.class))
                        .hasMessageContaining("Cannot convert 128 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(BigDecimal.valueOf(-129), Byte.class))
                        .hasMessageContaining("Cannot convert -129 to java.lang.Byte");
            }

            @Test
            void should_raise_error_when_value_has_decimal() {
                assertThatThrownBy(() -> numberType.convert(1.1F, Byte.class))
                        .hasMessageContaining("Cannot convert 1.1 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(1.1D, Byte.class))
                        .hasMessageContaining("Cannot convert 1.1 to java.lang.Byte");

                assertThatThrownBy(() -> numberType.convert(new BigDecimal("1.1"), Byte.class))
                        .hasMessageContaining("Cannot convert 1.1 to java.lang.Byte");
            }
        }

        @Nested
        class ConvertToShort {

            @Test
            void convert_to_short_with_out_error() {
                assertThat(numberType.convert((byte) 1, short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Byte.valueOf((byte) 1), short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert((short) 1, short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Short.valueOf("1"), short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert(1, short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Integer.valueOf("1"), short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert(1L, short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Long.valueOf("1"), short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert(1.0F, short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Float.valueOf("1.0"), short.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(1.0D, short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Double.valueOf("1.0"), short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert(BigInteger.valueOf(1), short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert(BigDecimal.valueOf(1), short.class)).isEqualTo((short) 1);
            }

            @Test
            void should_raise_error_when_value_over_flow() {
                assertThatThrownBy(() -> numberType.convert(32768, short.class))
                        .hasMessageContaining("Cannot convert 32768 to short");

                assertThatThrownBy(() -> numberType.convert(-32769, short.class))
                        .hasMessageContaining("Cannot convert -32769 to short");

                assertThatThrownBy(() -> numberType.convert(32768L, short.class))
                        .hasMessageContaining("Cannot convert 32768 to short");

                assertThatThrownBy(() -> numberType.convert(-32769L, short.class))
                        .hasMessageContaining("Cannot convert -32769 to short");

                assertThatThrownBy(() -> numberType.convert(32768.0F, short.class))
                        .hasMessageContaining("Cannot convert 32768.0 to short");

                assertThatThrownBy(() -> numberType.convert(-32769.0F, short.class))
                        .hasMessageContaining("Cannot convert -32769.0 to short");

                assertThatThrownBy(() -> numberType.convert(32768.0D, short.class))
                        .hasMessageContaining("Cannot convert 32768.0 to short");

                assertThatThrownBy(() -> numberType.convert(-32769.0D, short.class))
                        .hasMessageContaining("Cannot convert -32769.0 to short");

                assertThatThrownBy(() -> numberType.convert(BigInteger.valueOf(32768), short.class))
                        .hasMessageContaining("Cannot convert 32768 to short");

                assertThatThrownBy(() -> numberType.convert(BigInteger.valueOf(-32769), short.class))
                        .hasMessageContaining("Cannot convert -32769 to short");

                assertThatThrownBy(() -> numberType.convert(BigDecimal.valueOf(32768), short.class))
                        .hasMessageContaining("Cannot convert 32768 to short");

                assertThatThrownBy(() -> numberType.convert(BigDecimal.valueOf(-32769), short.class))
                        .hasMessageContaining("Cannot convert -32769 to short");
            }

            @Test
            void should_raise_error_when_value_has_decimal() {
                assertThatThrownBy(() -> numberType.convert(1.1F, short.class))
                        .hasMessageContaining("Cannot convert 1.1 to short");

                assertThatThrownBy(() -> numberType.convert(1.1D, short.class))
                        .hasMessageContaining("Cannot convert 1.1 to short");

                assertThatThrownBy(() -> numberType.convert(new BigDecimal("1.1"), short.class))
                        .hasMessageContaining("Cannot convert 1.1 to short");
            }
        }

        @Nested
        class ConvertToBoxedShort {

            @Test
            void convert_to_short_with_out_error() {
                assertThat(numberType.convert((byte) 1, Short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Byte.valueOf((byte) 1), Short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert((short) 1, Short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Short.valueOf("1"), Short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert(1, Short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Integer.valueOf("1"), Short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert(1L, Short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Long.valueOf("1"), Short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert(1.0F, Short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Float.valueOf("1.0"), Short.class)).isEqualTo((byte) 1);

                assertThat(numberType.convert(1.0D, Short.class)).isEqualTo((short) 1);
                assertThat(numberType.convert(Double.valueOf("1.0"), Short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert(BigInteger.valueOf(1), Short.class)).isEqualTo((short) 1);

                assertThat(numberType.convert(BigDecimal.valueOf(1), Short.class)).isEqualTo((short) 1);
            }

            @Test
            void should_raise_error_when_value_over_flow() {
                assertThatThrownBy(() -> numberType.convert(32768, Short.class))
                        .hasMessageContaining("Cannot convert 32768 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(-32769, Short.class))
                        .hasMessageContaining("Cannot convert -32769 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(32768L, Short.class))
                        .hasMessageContaining("Cannot convert 32768 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(-32769L, Short.class))
                        .hasMessageContaining("Cannot convert -32769 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(32768.0F, Short.class))
                        .hasMessageContaining("Cannot convert 32768.0 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(-32769.0F, Short.class))
                        .hasMessageContaining("Cannot convert -32769.0 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(32768.0D, Short.class))
                        .hasMessageContaining("Cannot convert 32768.0 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(-32769.0D, Short.class))
                        .hasMessageContaining("Cannot convert -32769.0 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(BigInteger.valueOf(32768), Short.class))
                        .hasMessageContaining("Cannot convert 32768 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(BigInteger.valueOf(-32769), Short.class))
                        .hasMessageContaining("Cannot convert -32769 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(BigDecimal.valueOf(32768), Short.class))
                        .hasMessageContaining("Cannot convert 32768 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(BigDecimal.valueOf(-32769), Short.class))
                        .hasMessageContaining("Cannot convert -32769 to java.lang.Short");
            }

            @Test
            void should_raise_error_when_value_has_decimal() {
                assertThatThrownBy(() -> numberType.convert(1.1F, Short.class))
                        .hasMessageContaining("Cannot convert 1.1 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(1.1D, Short.class))
                        .hasMessageContaining("Cannot convert 1.1 to java.lang.Short");

                assertThatThrownBy(() -> numberType.convert(new BigDecimal("1.1"), Short.class))
                        .hasMessageContaining("Cannot convert 1.1 to java.lang.Short");
            }
        }
    }

    public abstract class UnexpectedNumber extends Number {
    }
}