package com.github.leeonky.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class Converter {
    private static Consumer<Converter> defaultConverterConfig = (c) -> {
    };
    private TypeHandlerSet<Function> typeConverterSet = new TypeHandlerSet<>();
    private TypeHandlerSet<BiFunction> enumConverterSet = new TypeHandlerSet<>();

    public static Class<?> boxedClass(Class<?> source) {
        if (source.isPrimitive())
            if (source == int.class)
                return Integer.class;
            else if (source == short.class)
                return Short.class;
            else if (source == long.class)
                return Long.class;
            else if (source == float.class)
                return Float.class;
            else if (source == double.class)
                return Double.class;
            else if (source == boolean.class)
                return Boolean.class;
        return source;
    }

    public static Converter createDefault() {
        Converter converter = new Converter()
                .addTypeConverter(Object.class, String.class, Object::toString)
                .addTypeConverter(String.class, long.class, Long::valueOf)
                .addTypeConverter(String.class, int.class, Integer::valueOf)
                .addTypeConverter(String.class, short.class, Short::valueOf)
                .addTypeConverter(String.class, byte.class, Byte::valueOf)
                .addTypeConverter(String.class, double.class, Double::valueOf)
                .addTypeConverter(String.class, float.class, Float::valueOf)
                .addTypeConverter(String.class, boolean.class, Boolean::valueOf)

                .addTypeConverter(String.class, Long.class, Long::valueOf)
                .addTypeConverter(String.class, Integer.class, Integer::valueOf)
                .addTypeConverter(String.class, Short.class, Short::valueOf)
                .addTypeConverter(String.class, Byte.class, Byte::valueOf)
                .addTypeConverter(String.class, Double.class, Double::valueOf)
                .addTypeConverter(String.class, Float.class, Float::valueOf)
                .addTypeConverter(String.class, Boolean.class, Boolean::valueOf)

                .addTypeConverter(String.class, BigInteger.class, BigInteger::new)
                .addTypeConverter(String.class, BigDecimal.class, BigDecimal::new)

                .addTypeConverter(String.class, UUID.class, UUID::fromString)

                .addTypeConverter(String.class, Instant.class, Instant::parse)
                .addTypeConverter(String.class, Date.class, source -> {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd").parse(source);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Cannot convert '" + source + "' to " + Date.class.getName(), e);
                    }
                })
                .addTypeConverter(String.class, LocalTime.class, LocalTime::parse)
                .addTypeConverter(String.class, LocalDate.class, LocalDate::parse)
                .addTypeConverter(String.class, LocalDateTime.class, LocalDateTime::parse)
                .addTypeConverter(Long.class, BigDecimal.class, BigDecimal::new)
                .addTypeConverter(Integer.class, BigDecimal.class, BigDecimal::new)
                .addTypeConverter(Short.class, BigDecimal.class, BigDecimal::new)
                .addTypeConverter(Byte.class, BigDecimal.class, BigDecimal::new)
                .addTypeConverter(Float.class, BigDecimal.class, BigDecimal::new)
                .addTypeConverter(Double.class, BigDecimal.class, BigDecimal::new)
                .addTypeConverter(String.class, OffsetDateTime.class, OffsetDateTime::parse)
                .addTypeConverter(String.class, ZonedDateTime.class, ZonedDateTime::parse);
        defaultConverterConfig.accept(converter);
        return converter;
    }

    public static void configDefaultConverter(Consumer<Converter> config) {
        defaultConverterConfig = config;
    }

    public <T, R> Converter addTypeConverter(Class<T> source, Class<R> target, Function<T, R> converter) {
        typeConverterSet.add(boxedClass(source), target, converter);
        return this;
    }

    public Object tryConvert(Class<?> target, Object value) {
        return value == null ? null : target.isAssignableFrom(value.getClass()) ? value : convert(value.getClass(), target, value);
    }

    @SuppressWarnings("unchecked")
    private Object convert(Class<?> source, Class<?> target, Object value) {
        return typeConverterSet.findHandler(source, target, Collections::emptyList)
                .map(c -> c.getHandler().apply(value))
                .orElseGet(() -> target.isEnum() ? convertEnum(source, target, value) : value);
    }

    @SuppressWarnings("unchecked")
    private Object convertEnum(Class<?> source, Class<?> target, Object value) {
        return enumConverterSet.findHandler(source, target)
                .map(c -> c.getHandler().apply(target, value))
                .orElseGet(() -> Enum.valueOf((Class) target, value.toString()));
    }

    public <E, V> Converter addEnumConverter(Class<V> source, Class<E> target, BiFunction<Class<E>, V, E> converter) {
        enumConverterSet.add(boxedClass(source), target, converter);
        return this;
    }
}
