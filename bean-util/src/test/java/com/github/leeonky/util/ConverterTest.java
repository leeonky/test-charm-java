package com.github.leeonky.util;

import lombok.Getter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static com.github.leeonky.util.Converter.getInstance;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConverterTest {
    private Converter converter = new Converter();

    public static class NumberString {
        public String string;
    }

    @Test
    void support_convert_null() {
        assertThat(converter.tryConvert(String.class, null)).isNull();
    }

    @Test
    void should_use_more_close_type_handler() {
        converter.addTypeConverter(Object.class, String.class, value -> "object to string");
        converter.addTypeConverter(BaseInterface.class, String.class, value -> "not object to string");

        assertThat(converter.tryConvert(String.class, new TargetClass())).isEqualTo("not object to string");
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

    public interface BaseInterface {
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

    public static class TargetClass implements BaseInterface {
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

            assertConvert(YearMonth.class, "2000-10", YearMonth.parse("2000-10"));
        }

        private void assertConvert(Class<?> type, Object value, Object toValue) {
            assertThat(converter.tryConvert(type, value)).isEqualTo(toValue);
        }

        @Test
        void support_convert_string_in_offset_date_time_to_instant() {
            assertConvert(Instant.class, "1996-01-23T08:00:01+08:00", Instant.parse("1996-01-23T00:00:01Z"));
        }

        @Test
        void register_config() {
            Converter converter = Converter.createDefault().addTypeConverter(Type.class, String.class, t -> "customer converter");
            assertThat(converter.tryConvert(String.class, new Type())).isEqualTo("customer converter");

        }

        @Test
        void should_raise_error_when_invalid_date_format() {
            assertThrows(IllegalArgumentException.class, () -> Converter.createDefault().tryConvert(Date.class, "invalid date"));
        }

        @Test
        void support_replace_default_converter() {
            int fixInt = 1000;
            converter.addTypeConverter(String.class, Integer.class, str -> fixInt);

            assertThat(converter.convert(Integer.class, "2000")).isEqualTo(1000);
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

        @Test
        void convert_to_number() {
            converter.addTypeConverter(NumberString.class, Number.class, n -> new NumberParser().parse(n.string));
            assertThat(converter.convert(Number.class, new NumberString() {{
                string = "1000L";
            }})).isEqualTo(1000L);
        }
    }

    @Nested
    class BooleanCovert {

        @Test
        void convert_to_primitive_type() {
            assertThat(getInstance().convert(boolean.class, true)).isTrue();
        }
    }

    @Nested
    class Collection {

        @Test
        void convert_empty_to_empty_with_right_type() {
            assertThat((String[]) converter.tryConvert(String[].class, emptyList())).isEmpty();
            assertThat((Set<?>) converter.tryConvert(Set.class, emptyList())).isEmpty();
        }

        @Test
        @SuppressWarnings("unchecked")
        void support_convert_collection() {
            assertThat((String[]) converter.tryConvert(String[].class, asList("A", "B"))).containsExactly("A", "B");
            assertThat((Set<String>) converter.tryConvert(Set.class, asList("A", "B")))
                    .isInstanceOf(Set.class)
                    .containsExactly("A", "B");
            assertThat((LinkedHashSet<String>) converter.tryConvert(LinkedHashSet.class, asList("A", "B")))
                    .isInstanceOf(LinkedHashSet.class)
                    .containsExactly("A", "B");
        }

        @Test
        void raise_convert_error_when_not_supported() {
            assertThatThrownBy(() -> converter.convert(ArrayList.class, new Object()))
                    .hasMessage("Cannot convert from java.lang.Object to class java.util.ArrayList")
                    .hasCauseExactlyInstanceOf(CannotToStreamException.class);
        }
    }

    @Nested
    class CanConvert {

        class Super {
        }

        class Sub extends Super {

        }

        @Test
        void not_supported() {
            assertThat(converter.supported(Object.class, ConverterTest.class)).isFalse();
        }

        @Nested
        class Supported {

            @Test
            void from_sub_to_super() {
                assertThat(converter.supported(Sub.class, Super.class)).isTrue();
            }

            @Test
            void register_convertor() {
                converter.addTypeConverter(Object.class, ConverterTest.class, o -> null);

                assertThat(converter.supported(Object.class, ConverterTest.class)).isTrue();
            }

            @Test
            void target_is_enum() {
                assertThat(converter.supported(Object.class, TargetEnum.class)).isTrue();
            }

            @Test
            void target_is_collection_and_source_is_array() {
                assertThat(converter.supported(String[].class, List.class)).isTrue();
            }

            @Test
            void target_is_collection_and_source_is_iterable() {
                assertThat(converter.supported(List.class, String[].class)).isTrue();
            }

            @Test
            void target_is_collection_and_source_is_stream() {
                assertThat(converter.supported(Stream.class, String[].class)).isTrue();
            }
        }
    }

    public enum TargetEnum {
    }
}
