package com.github.leeonky.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.leeonky.util.BeanClass.create;
import static com.github.leeonky.util.Classes.getClassName;

public class Converter {
    private static final NumberType numberType = new NumberType();
    private static Converter instance;
    private final TypeHandlerSet<Function<Object, Object>> typeConverterSet = new TypeHandlerSet<>();
    private final TypeHandlerSet<BiFunction<Class<? extends Enum<?>>, Object, Object>> enumConverterSet = new TypeHandlerSet<>();

    public static Converter getInstance() {
        if (instance == null)
            instance = ConverterFactory.create();
        return instance;
    }

    public static Converter createDefault() {
        return new Converter()
                .addTypeConverter(Object.class, String.class, Object::toString)
                .addTypeConverter(String.class, Long.class, Long::valueOf)
                .addTypeConverter(String.class, long.class, Long::valueOf)
                .addTypeConverter(String.class, Integer.class, Integer::valueOf)
                .addTypeConverter(String.class, int.class, Integer::valueOf)
                .addTypeConverter(String.class, Short.class, Short::valueOf)
                .addTypeConverter(String.class, short.class, Short::valueOf)
                .addTypeConverter(String.class, Byte.class, Byte::valueOf)
                .addTypeConverter(String.class, byte.class, Byte::valueOf)
                .addTypeConverter(String.class, Double.class, Double::valueOf)
                .addTypeConverter(String.class, double.class, Double::valueOf)
                .addTypeConverter(String.class, Float.class, Float::valueOf)
                .addTypeConverter(String.class, float.class, Float::valueOf)
                .addTypeConverter(String.class, Boolean.class, Boolean::valueOf)
                .addTypeConverter(String.class, boolean.class, Boolean::valueOf)
                .addTypeConverter(Boolean.class, boolean.class, Boolean::booleanValue)
                .addTypeConverter(String.class, BigInteger.class, BigInteger::new)
                .addTypeConverter(String.class, BigDecimal.class, BigDecimal::new)
                .addTypeConverter(String.class, UUID.class, UUID::fromString)
                .addTypeConverter(String.class, Instant.class, Converter::parseToInstant)
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
                .addTypeConverter(String.class, OffsetDateTime.class, OffsetDateTime::parse)
                .addTypeConverter(String.class, ZonedDateTime.class, ZonedDateTime::parse)
                .addTypeConverter(String.class, YearMonth.class, YearMonth::parse)
                .addTypeConverter(Number.class, Byte.class, numberType::byteValue)
                .addTypeConverter(Number.class, byte.class, numberType::byteValue)
                .addTypeConverter(Number.class, Short.class, numberType::shortValue)
                .addTypeConverter(Number.class, short.class, numberType::shortValue)
                .addTypeConverter(Number.class, Integer.class, numberType::intValue)
                .addTypeConverter(Number.class, int.class, numberType::intValue)
                .addTypeConverter(Number.class, Long.class, numberType::longValue)
                .addTypeConverter(Number.class, long.class, numberType::longValue)
                .addTypeConverter(Number.class, Double.class, numberType::doubleValue)
                .addTypeConverter(Number.class, double.class, numberType::doubleValue)
                .addTypeConverter(Number.class, Float.class, numberType::floatValue)
                .addTypeConverter(Number.class, float.class, numberType::floatValue)
                .addTypeConverter(Number.class, BigDecimal.class, numberType::bigDecimalValue)
                .addTypeConverter(Number.class, BigInteger.class, numberType::bigIntegerValue);
    }

    private static Instant parseToInstant(String charSequence) {
        try {
            return Instant.parse(charSequence);
        } catch (DateTimeParseException ignore) {
            return OffsetDateTime.parse(charSequence).toInstant();
        }
    }

    @SuppressWarnings("unchecked")
    public <T, R> Converter addTypeConverter(Class<T> source, Class<R> target, Function<T, R> converter) {
        typeConverterSet.add(NumberType.boxedClass(source), target, (Function<Object, Object>) converter);
        return this;
    }

    public Object tryConvert(Class<?> target, Object value) {
        return convert(target, value, Function.identity());
    }

    private <T> Object convert(Class<T> target, Object value, Function<Object, Object> defaultValue) {
        if (value == null)
            return null;
        Class<?> source = value.getClass();
        if (target.isAssignableFrom(source))
            return value;
        return typeConverterSet.findHandler(source, target, Collections::emptyList)
                .map(c -> c.getHandler().apply(value))
                .orElseGet(() -> defaultConvert(target, value, defaultValue, source));
    }

    @SuppressWarnings("unchecked")
    private <T> Object defaultConvert(Class<T> target, Object value, Function<Object, Object> defaultValue, Class<?> source) {
        if (target.isEnum())
            return convertEnum(source, (Class<? extends Enum>) target, value);
        if (value != null) {
            BeanClass<T> targetBean = create(target);
            if (targetBean.isCollection())
                try {
                    return CollectionHelper.convert(value, targetBean, this);
                } catch (CannotToStreamException e) {
                    throw new ConvertException(String.format("Cannot convert from %s to %s", getClassName(value), target), e);
                }
        }
        return defaultValue.apply(value);
    }

    private <E extends Enum<E>> Object convertEnum(Class<?> source, Class<E> target, Object value) {
        return enumConverterSet.findHandler(source, target)
                .map(c -> c.getHandler().apply(target, value))
                .orElseGet(() -> Enum.valueOf(target, value.toString()));
    }

    @SuppressWarnings("unchecked")
    public <E extends Enum<E>, V> Converter addEnumConverter(Class<V> source, Class<E> target,
                                                             BiFunction<Class<E>, V, E> converter) {
        enumConverterSet.add(NumberType.boxedClass(source), target, (BiFunction) converter);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T convert(Class<T> target, Object value) {
        return (T) convert(target, value, v -> {
            throw new ConvertException(String.format("Cannot convert from %s to %s", getClassName(value), target));
        });
    }

    public Converter extend() {
        Classes.subTypesOf(ConverterExtension.class, "com.github.leeonky.util.extensions")
                .forEach(c -> Classes.newInstance(c).extend(this));
        Classes.subTypesOf(ConverterExtension.class, "com.github.leeonky.extensions.util")
                .forEach(c -> Classes.newInstance(c).extend(this));
        return this;
    }

    public boolean supported(Class<?> source, Class<?> target) {
        if (target.isAssignableFrom(source))
            return true;
        if (typeConverterSet.findHandler(source, target, Collections::emptyList).isPresent())
            return true;
        if (target.isEnum())
            return true;
        if (create(target).isCollection())
            return source.isArray() || Iterable.class.isAssignableFrom(source) || Stream.class.isAssignableFrom(source);
        return false;
    }
}
