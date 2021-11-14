package com.github.leeonky.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;

public class NumberUtil {
    private static final List<Class<?>> NUMBER_TYPES = asList(Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, BigInteger.class, BigDecimal.class);
    private static Converter converter = Converter.INSTANCE;

    public static Class<?> calculationType(Class<?> type1, Class<?> type2) {
        Class<?> boxedType1 = BeanClass.boxedClass(type1);
        Class<?> boxedType2 = BeanClass.boxedClass(type2);
        if (isFloatAndBigInteger(boxedType1, boxedType2) || isFloatAndBigInteger(boxedType2, boxedType1))
            return BigDecimal.class;
        return NUMBER_TYPES.indexOf(boxedType1) > NUMBER_TYPES.indexOf(boxedType2) ? boxedType1 : boxedType2;
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
            return (byte) leftInSameType + (byte) rightInSameType;
        if (type.equals(Short.class))
            return (short) leftInSameType + (short) rightInSameType;
        if (type.equals(Integer.class))
            return (int) leftInSameType + (int) rightInSameType;
        if (type.equals(Long.class))
            return (long) leftInSameType + (long) rightInSameType;
        if (type.equals(Float.class))
            return (float) leftInSameType + (float) rightInSameType;
        if (type.equals(Double.class))
            return (double) leftInSameType + (double) rightInSameType;
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
            return (byte) leftInSameType - (byte) rightInSameType;
        if (type.equals(Short.class))
            return (short) leftInSameType - (short) rightInSameType;
        if (type.equals(Integer.class))
            return (int) leftInSameType - (int) rightInSameType;
        if (type.equals(Long.class))
            return (long) leftInSameType - (long) rightInSameType;
        if (type.equals(Float.class))
            return (float) leftInSameType - (float) rightInSameType;
        if (type.equals(Double.class))
            return (double) leftInSameType - (double) rightInSameType;
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
            return (byte) leftInSameType / (byte) rightInSameType;
        if (type.equals(Short.class))
            return (short) leftInSameType / (short) rightInSameType;
        if (type.equals(Integer.class))
            return (int) leftInSameType / (int) rightInSameType;
        if (type.equals(Long.class))
            return (long) leftInSameType / (long) rightInSameType;
        if (type.equals(Float.class))
            return (float) leftInSameType / (float) rightInSameType;
        if (type.equals(Double.class))
            return (double) leftInSameType / (double) rightInSameType;
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
            return (byte) leftInSameType * (byte) rightInSameType;
        if (type.equals(Short.class))
            return (short) leftInSameType * (short) rightInSameType;
        if (type.equals(Integer.class))
            return (int) leftInSameType * (int) rightInSameType;
        if (type.equals(Long.class))
            return (long) leftInSameType * (long) rightInSameType;
        if (type.equals(Float.class))
            return (float) leftInSameType * (float) rightInSameType;
        if (type.equals(Double.class))
            return (double) leftInSameType * (double) rightInSameType;
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
            return Byte.compare((byte) leftInSameType, (byte) rightInSameType);
        if (type.equals(Short.class))
            return Short.compare((short) leftInSameType, (short) rightInSameType);
        if (type.equals(Integer.class))
            return Integer.compare((int) leftInSameType, (int) rightInSameType);
        if (type.equals(Long.class))
            return Long.compare((long) leftInSameType, (long) rightInSameType);
        if (type.equals(Float.class))
            return Float.compare((float) leftInSameType, (float) rightInSameType);
        if (type.equals(Double.class))
            return Double.compare((double) leftInSameType, (double) rightInSameType);
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

    public static Number negate(Number left) {
        Class<?> type = BeanClass.boxedClass(left.getClass());
        if (type.equals(Byte.class))
            return (byte) -(byte) left;
        if (type.equals(Short.class))
            return (short) -(short) left;
        if (type.equals(Integer.class))
            return -(int) left;
        if (type.equals(Long.class))
            return -(long) left;
        if (type.equals(Float.class))
            return -(float) left;
        if (type.equals(Double.class))
            return -(double) left;
        if (type.equals(BigInteger.class))
            return ((BigInteger) left).negate();
        if (type.equals(BigDecimal.class))
            return ((BigDecimal) left).negate();
        throw new IllegalArgumentException("unsupported type " + type);
    }
}
