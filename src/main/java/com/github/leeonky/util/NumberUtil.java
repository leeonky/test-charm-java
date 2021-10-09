package com.github.leeonky.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;

public class NumberUtil {
    private static final List<Class<?>> NUMBER_TYPES = asList(Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, BigInteger.class, BigDecimal.class);
    private static Converter converter = Converter.createDefault();

    public static Class<?> calculationType(Class<?> type1, Class<?> type2) {
        Class<?> boxedType1 = boxedClass(type1);
        Class<?> boxedType2 = boxedClass(type2);
        if (isFloatAndBigInteger(boxedType1, boxedType2) || isFloatAndBigInteger(boxedType2, boxedType1))
            return BigDecimal.class;
        return NUMBER_TYPES.indexOf(boxedType1) > NUMBER_TYPES.indexOf(boxedType2) ? boxedType1 : boxedType2;
    }

    public static Class<?> boxedClass(Class<?> source) {
        if (source.isPrimitive())
            if (source == char.class)
                return Character.class;
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

    private static boolean isFloatAndBigInteger(Class<?> boxedType1, Class<?> boxedType2) {
        return boxedType1.equals(BigInteger.class) && (boxedType2.equals(Float.class) || boxedType2.equals(Double.class));
    }

    public static Number plus(Number left, Number right) {
        return plus(left, right, converter);
    }

    public static Number plus(Number left, Number right, Converter converter) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        return plus((Number) converter.tryConvert(type, left), (Number) converter.tryConvert(type, right), type);
    }

    private static Number plus(Number leftInSameType, Number rightInSameType, Class<?> type) {
        if (type.equals(Byte.class))
            return (Byte) leftInSameType + (Byte) rightInSameType;
        if (type.equals(Short.class))
            return (Short) leftInSameType + (Short) rightInSameType;
        if (type.equals(Integer.class))
            return (Integer) leftInSameType + (Integer) rightInSameType;
        if (type.equals(Long.class))
            return (Long) leftInSameType + (Long) rightInSameType;
        if (type.equals(Float.class))
            return (Float) leftInSameType + (Float) rightInSameType;
        if (type.equals(Double.class))
            return (Double) leftInSameType + (Double) rightInSameType;
        if (type.equals(BigInteger.class))
            return ((BigInteger) leftInSameType).add((BigInteger) rightInSameType);
        if (type.equals(BigDecimal.class))
            return ((BigDecimal) leftInSameType).add((BigDecimal) rightInSameType);
        throw new IllegalArgumentException("unsupported type " + type);
    }

    public static Number subtract(Number left, Number right) {
        return subtract(left, right, converter);
    }

    public static Number subtract(Number left, Number right, Converter converter) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        return subtract((Number) converter.tryConvert(type, left), (Number) converter.tryConvert(type, right), type);
    }

    private static Number subtract(Number leftInSameType, Number rightInSameType, Class<?> type) {
        if (type.equals(Byte.class))
            return (Byte) leftInSameType - (Byte) rightInSameType;
        if (type.equals(Short.class))
            return (Short) leftInSameType - (Short) rightInSameType;
        if (type.equals(Integer.class))
            return (Integer) leftInSameType - (Integer) rightInSameType;
        if (type.equals(Long.class))
            return (Long) leftInSameType - (Long) rightInSameType;
        if (type.equals(Float.class))
            return (Float) leftInSameType - (Float) rightInSameType;
        if (type.equals(Double.class))
            return (Double) leftInSameType - (Double) rightInSameType;
        if (type.equals(BigInteger.class))
            return ((BigInteger) leftInSameType).subtract((BigInteger) rightInSameType);
        if (type.equals(BigDecimal.class))
            return ((BigDecimal) leftInSameType).subtract((BigDecimal) rightInSameType);
        throw new IllegalArgumentException("unsupported type " + type);
    }

    public static Number divide(Number left, Number right) {
        return divide(left, right, converter);
    }

    public static Number divide(Number left, Number right, Converter converter) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        return divide((Number) converter.tryConvert(type, left), (Number) converter.tryConvert(type, right), type);
    }

    private static Number divide(Number leftInSameType, Number rightInSameType, Class<?> type) {
        if (type.equals(Byte.class))
            return (Byte) leftInSameType / (Byte) rightInSameType;
        if (type.equals(Short.class))
            return (Short) leftInSameType / (Short) rightInSameType;
        if (type.equals(Integer.class))
            return (Integer) leftInSameType / (Integer) rightInSameType;
        if (type.equals(Long.class))
            return (Long) leftInSameType / (Long) rightInSameType;
        if (type.equals(Float.class))
            return (Float) leftInSameType / (Float) rightInSameType;
        if (type.equals(Double.class))
            return (Double) leftInSameType / (Double) rightInSameType;
        if (type.equals(BigInteger.class))
            return ((BigInteger) leftInSameType).divide((BigInteger) rightInSameType);
        if (type.equals(BigDecimal.class))
            return ((BigDecimal) leftInSameType).divide((BigDecimal) rightInSameType);
        throw new IllegalArgumentException("unsupported type " + type);
    }

    public static Number multiply(Number left, Number right) {
        return multiply(left, right, converter);
    }

    public static Number multiply(Number left, Number right, Converter converter) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        return multiply((Number) converter.tryConvert(type, left), (Number) converter.tryConvert(type, right), type);
    }

    private static Number multiply(Number leftInSameType, Number rightInSameType, Class<?> type) {
        if (type.equals(Byte.class))
            return (Byte) leftInSameType * (Byte) rightInSameType;
        if (type.equals(Short.class))
            return (Short) leftInSameType * (Short) rightInSameType;
        if (type.equals(Integer.class))
            return (Integer) leftInSameType * (Integer) rightInSameType;
        if (type.equals(Long.class))
            return (Long) leftInSameType * (Long) rightInSameType;
        if (type.equals(Float.class))
            return (Float) leftInSameType * (Float) rightInSameType;
        if (type.equals(Double.class))
            return (Double) leftInSameType * (Double) rightInSameType;
        if (type.equals(BigInteger.class))
            return ((BigInteger) leftInSameType).multiply((BigInteger) rightInSameType);
        if (type.equals(BigDecimal.class))
            return ((BigDecimal) leftInSameType).multiply((BigDecimal) rightInSameType);
        throw new IllegalArgumentException("unsupported type " + type);
    }

    public static int compare(Number left, Number right) {
        return compare(left, right, converter);
    }

    public static int compare(Number left, Number right, Converter converter) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        return compare((Number) converter.tryConvert(type, left), (Number) converter.tryConvert(type, right), type);
    }

    private static int compare(Number leftInSameType, Number rightInSameType, Class<?> type) {
        if (type.equals(Byte.class))
            return Byte.compare((Byte) leftInSameType, (Byte) rightInSameType);
        if (type.equals(Short.class))
            return Short.compare((Short) leftInSameType, (Short) rightInSameType);
        if (type.equals(Integer.class))
            return Integer.compare((Integer) leftInSameType, (Integer) rightInSameType);
        if (type.equals(Long.class))
            return Long.compare((Long) leftInSameType, (Long) rightInSameType);
        if (type.equals(Float.class))
            return Float.compare((Float) leftInSameType, (Float) rightInSameType);
        if (type.equals(Double.class))
            return Double.compare((Double) leftInSameType, (Double) rightInSameType);
        if (type.equals(BigInteger.class))
            return ((BigInteger) leftInSameType).compareTo((BigInteger) rightInSameType);
        if (type.equals(BigDecimal.class))
            return ((BigDecimal) leftInSameType).compareTo((BigDecimal) rightInSameType);
        throw new IllegalArgumentException("unsupported type " + type);
    }

    public static Converter getConverter() {
        return converter;
    }

    public static void setConverter(Converter converter) {
        NumberUtil.converter = converter;
    }
}
