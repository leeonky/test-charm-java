package com.github.leeonky.util;

import lombok.Getter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConverterTest {
    private Converter converter = new Converter();

    @Test
    void support_convert_null() {
        assertThat(converter.tryConvert(String.class, null)).isNull();
    }

    enum NameEnums {
        E1, E2
    }

    enum ValueEnums implements ValueEnum<Integer> {
        E1(0), E2(1);

        @Getter
        Integer value;

        ValueEnums(int i) {
            value = i;
        }
    }

    interface ValueEnum<V extends Number> {
        static <E extends ValueEnum<V>, V extends Number> E fromValue(Class<E> type, V value) {
            return Arrays.stream(type.getEnumConstants()).filter((v) -> v.getValue().equals(value))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Unsupported enum value '" + value + "'"));
        }

        static <E extends ValueEnum<V>, V extends Number> E fromNumber(Class<E> type, Number value) {
            return Arrays.stream(type.getEnumConstants()).filter((v) -> v.getValue().equals(value))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Unsupported enum value '" + value + "'"));
        }

        V getValue();
    }

    public static class Type {

    }

    @Nested
    class TypeHandler {
        @Test
        void no_candidate_converter_should_return_original_value() {
            converter.addTypeConverter(Long.class, Bean.class, s -> null);

            assertThat(converter.tryConvert(Bean.class, "String")).isEqualTo("String");
        }

        @Test
        void no_defined_converter_for_target_type_should_return_original_value() {
            assertThat(converter.tryConvert(Bean.class, "String")).isEqualTo("String");
        }

        @Test
        void assign_sub_type_to_base_should_keep_original() {
            Bean.SubBean subBean = new Bean.SubBean();
            converter.addTypeConverter(Bean.SubBean.class, Bean.class, sb -> {
                throw new RuntimeException();
            });

            assertThat(converter.tryConvert(Bean.class, subBean)).isEqualTo(subBean);
        }

        @Test
        void convert_via_registered_converter() {
            converter.addTypeConverter(String.class, Integer.class, Integer::valueOf);

            assertThat(converter.tryConvert(Integer.class, "100")).isEqualTo(100);
        }

        @Test
        void convert_via_registered_converter_as_base_type_matches() {
            converter.addTypeConverter(Object.class, String.class, o -> "Hello");

            assertThat(converter.tryConvert(String.class, new Bean())).isEqualTo("Hello");
        }
    }

    @Nested
    class EnumConvert {
        @Test
        void covert_to_enum_from_name() {
            assertThat(converter.tryConvert(NameEnums.class, "E2")).isEqualTo(NameEnums.E2);
        }

        @Test
        void convert_to_customer_enum_with_enum_type() {
            converter.addEnumConverter(Integer.class, ValueEnums.class, ValueEnum::fromValue);

            assertThat(converter.tryConvert(ValueEnums.class, 1)).isEqualTo(ValueEnums.E2);
        }

        @Test
        void convert_to_customer_enum_with_enum_type_auto_boxed() {
            converter.addEnumConverter(int.class, ValueEnums.class, ValueEnum::fromValue);

            assertThat(converter.tryConvert(ValueEnums.class, 1)).isEqualTo(ValueEnums.E2);
        }

        @Test
        void convert_to_customer_enum_with_enum_type_and_sub_value_type() {
            converter.addEnumConverter(Number.class, ValueEnums.class, ValueEnum::fromNumber);

            assertThat(converter.tryConvert(ValueEnums.class, 1)).isEqualTo(ValueEnums.E2);
        }

        @Test
        void convert_to_customer_enum_with_enum_base_type() {
            converter.addEnumConverter(Integer.class, Enum.class, (c, i) -> ValueEnums.E2);

            assertThat(converter.tryConvert(ValueEnums.class, 1)).isEqualTo(ValueEnums.E2);
        }
    }

    @Nested
    class DefaultConvert {
        Converter converter = Converter.createDefault();

        @Test
        void parse_string() throws ParseException {
            assertConvert(long.class, "100", 100L);
            assertConvert(int.class, "100", 100);
            assertConvert(short.class, "100", (short) 100);
            assertConvert(byte.class, "100", (byte) 100);
            assertConvert(float.class, "100", (float) 100);
            assertConvert(double.class, "100", (double) 100);
            assertConvert(boolean.class, "true", true);

            assertConvert(Long.class, "100", 100L);
            assertConvert(Integer.class, "100", 100);
            assertConvert(Short.class, "100", (short) 100);
            assertConvert(Byte.class, "100", (byte) 100);
            assertConvert(Float.class, "100", (float) 100);
            assertConvert(Double.class, "100", (double) 100);
            assertConvert(Boolean.class, "true", true);

            assertConvert(BigDecimal.class, "100", BigDecimal.valueOf(100));
            assertConvert(BigInteger.class, "100", BigInteger.valueOf(100));

            assertConvert(UUID.class, "123e4567-e89b-12d3-a456-426655440000", UUID.fromString("123e4567-e89b-12d3-a456-426655440000"));
            assertConvert(Instant.class, "2001-10-12T12:00:01.123Z", Instant.parse("2001-10-12T12:00:01.123Z"));
            assertConvert(Date.class, "2001-10-12", new SimpleDateFormat("yyyy-MM-dd").parse("2001-10-12"));
            assertConvert(LocalTime.class, "00:00:01", LocalTime.parse("00:00:01"));
            assertConvert(LocalDate.class, "1996-01-24", LocalDate.parse("1996-01-24"));
            assertConvert(LocalDateTime.class, "1996-01-23T00:00:01", LocalDateTime.parse("1996-01-23T00:00:01"));

            assertConvert(OffsetDateTime.class, "1996-01-23T00:00:01+08:00",
                    LocalDateTime.parse("1996-01-23T00:00:01").atOffset(ZoneOffset.of("+08:00")));

            assertConvert(ZonedDateTime.class, "2017-04-26T15:13:12.006+02:00[Europe/Paris]",
                    LocalDateTime.parse("2017-04-26T15:13:12.006").atZone(ZoneId.of("Europe/Paris")));

            assertConvert(ZonedDateTime.class, "1996-01-23T00:00:01+08:00",
                    LocalDateTime.parse("1996-01-23T00:00:01").atZone(ZoneOffset.of("+08:00")));
        }

        private void assertConvert(Class<?> type, Object value, Object toValue) {
            assertThat(converter.tryConvert(type, value)).isEqualTo(toValue);
        }

        @Test
        void register_config() {
            Converter.configDefaultConverter(converter -> converter.addTypeConverter(Type.class, String.class, t -> "customer converter"));
            assertThat(Converter.createDefault().tryConvert(String.class, new Type())).isEqualTo("customer converter");

        }

        @Test
        void should_raise_error_when_invalid_date_format() {
            assertThrows(IllegalArgumentException.class, () -> Converter.createDefault().tryConvert(Date.class, "invalid date"));
        }

        @Nested
        class NumberConvert {

            @Test
            void to_big_decimal() {
                assertConvert(BigDecimal.class, 100L, BigDecimal.valueOf(100));
                assertConvert(BigDecimal.class, 100, BigDecimal.valueOf(100));
                assertConvert(BigDecimal.class, (short) 100, BigDecimal.valueOf(100));
                assertConvert(BigDecimal.class, (byte) 100, BigDecimal.valueOf(100));
                assertConvert(BigDecimal.class, (float) 100, BigDecimal.valueOf(100.0));
                assertConvert(BigDecimal.class, (double) 100, BigDecimal.valueOf(100.0));
            }
        }
    }

    @Nested
    class ConvertWithException {

        @Test
        void same_type_convert() {
            assertThat(converter.convert(Integer.class, 1)).isEqualTo(Integer.valueOf(1));
        }

        @Test
        void sub_type_convert() {
            assertThat(converter.convert(CharSequence.class, "Hello")).isInstanceOf(CharSequence.class);
        }

        @Test
        void should_raise_error_when_can_not_convert() {
            assertThrows(ConvertException.class, () -> converter.convert(Integer.class, "hello"));
        }
    }

    @Nested
    class NumberConvert {
        private final List<Number> numbers = asList(new Number[]{
                (byte) 0, (short) 0, 0, 0L, 0.0d, 0.0f, new BigDecimal("0"), new BigInteger("0")
        });
        private Converter converter = Converter.createDefault();

        @Test
        void convent_to_byte() {
            numbers.forEach(number ->
                    assertThat(converter.tryConvert(Byte.class, number)).isEqualTo((byte) 0));

            numbers.forEach(number ->
                    assertThat(converter.tryConvert(byte.class, number)).isEqualTo((byte) 0));
        }

        @Test
        void convent_to_short() {
            numbers.forEach(number ->
                    assertThat(converter.tryConvert(Short.class, number)).isEqualTo((short) 0));

            numbers.forEach(number ->
                    assertThat(converter.tryConvert(short.class, number)).isEqualTo((short) 0));
        }

        @Test
        void convent_to_int() {
            numbers.forEach(number ->
                    assertThat(converter.tryConvert(Integer.class, number)).isEqualTo(0));

            numbers.forEach(number ->
                    assertThat(converter.tryConvert(int.class, number)).isEqualTo(0));
        }

        @Test
        void convent_to_long() {
            numbers.forEach(number ->
                    assertThat(converter.tryConvert(Long.class, number)).isEqualTo(0L));

            numbers.forEach(number ->
                    assertThat(converter.tryConvert(long.class, number)).isEqualTo(0L));
        }

        @Test
        void convent_to_double() {
            numbers.forEach(number ->
                    assertThat(converter.tryConvert(Double.class, number)).isEqualTo(0.0D));

            numbers.forEach(number ->
                    assertThat(converter.tryConvert(double.class, number)).isEqualTo(0.0D));
        }

        @Test
        void convent_to_float() {
            numbers.forEach(number ->
                    assertThat(converter.tryConvert(Float.class, number)).isEqualTo(0.0F));

            numbers.forEach(number ->
                    assertThat(converter.tryConvert(float.class, number)).isEqualTo(0.0F));
        }

        @Test
        void convent_to_big_decimal() {
            numbers.forEach(number -> assertThat(((BigDecimal) converter.tryConvert(BigDecimal.class, number))
                    .compareTo(new BigDecimal("0"))).isEqualTo(0));
        }

        @Test
        void convent_to_big_integer() {
            numbers.forEach(number ->
                    assertThat(converter.tryConvert(BigInteger.class, number)).isEqualTo(new BigInteger("0")));
        }
    }
}
