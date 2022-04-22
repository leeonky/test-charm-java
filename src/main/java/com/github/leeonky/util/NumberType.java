package com.github.leeonky.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;

public class NumberType {
    private static final List<Class<?>> NUMBER_TYPES = asList(Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, BigInteger.class, BigDecimal.class);
    private Converter converter = Converter.getInstance();
    private double doubleEpsilon = 0.0000001d;
    private float floatEpsilon = 0.000001f;

    public static Class<?> calculationType(Class<?> number1, Class<?> number2) {
        Class<?> boxedType1 = BeanClass.boxedClass(number1);
        Class<?> boxedType2 = BeanClass.boxedClass(number2);
        if (isFloatAndBigInteger(boxedType1, boxedType2) || isFloatAndBigInteger(boxedType2, boxedType1))
            return BigDecimal.class;
        return NUMBER_TYPES.indexOf(boxedType1) > NUMBER_TYPES.indexOf(boxedType2) ? boxedType1 : boxedType2;
    }

    private static boolean isFloatAndBigInteger(Class<?> boxedType1, Class<?> boxedType2) {
        return boxedType1.equals(BigInteger.class) && (boxedType2.equals(Float.class) || boxedType2.equals(Double.class));
    }

    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public Number plus(Number left, Number right) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        Number leftInSameType = (Number) converter.tryConvert(type, left);
        Number rightInSameType = (Number) converter.tryConvert(type, right);
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

    public Number subtract(Number left, Number right) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        Number leftInSameType = (Number) converter.tryConvert(type, left);
        Number rightInSameType = (Number) converter.tryConvert(type, right);
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

    public Number divide(Number left, Number right) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        Number leftInSameType = (Number) converter.tryConvert(type, left);
        Number rightInSameType = (Number) converter.tryConvert(type, right);
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

    public Number multiply(Number left, Number right) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        Number leftInSameType = (Number) converter.tryConvert(type, left);
        Number rightInSameType = (Number) converter.tryConvert(type, right);
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

    public int compare(Number left, Number right) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        Number leftInSameType = (Number) converter.tryConvert(type, left);
        Number rightInSameType = (Number) converter.tryConvert(type, right);
        if (type.equals(Byte.class))
            return Byte.compare((byte) leftInSameType, (byte) rightInSameType);
        if (type.equals(Short.class))
            return Short.compare((short) leftInSameType, (short) rightInSameType);
        if (type.equals(Integer.class))
            return Integer.compare((int) leftInSameType, (int) rightInSameType);
        if (type.equals(Long.class))
            return Long.compare((long) leftInSameType, (long) rightInSameType);
        if (type.equals(Float.class)) {
            float sub = (float) leftInSameType - (float) rightInSameType;
            if (sub > floatEpsilon)
                return 1;
            if (sub < -floatEpsilon)
                return -1;
            return 0;
        }
        if (type.equals(Double.class)) {
            double sub = (double) leftInSameType - (double) rightInSameType;
            if (sub > doubleEpsilon)
                return 1;
            if (sub < -doubleEpsilon)
                return -1;
            return 0;
        }
        if (type.equals(BigInteger.class))
            return ((BigInteger) leftInSameType).compareTo((BigInteger) rightInSameType);
        if (type.equals(BigDecimal.class))
            return ((BigDecimal) leftInSameType).compareTo((BigDecimal) rightInSameType);
        throw new IllegalArgumentException("unsupported type " + type);
    }

    public Number negate(Number left) {
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

    public double getDoubleEpsilon() {
        return doubleEpsilon;
    }

    public void setDoubleEpsilon(double doubleEpsilon) {
        this.doubleEpsilon = doubleEpsilon;
    }

    public float getFloatEpsilon() {
        return floatEpsilon;
    }

    public void setFloatEpsilon(float floatEpsilon) {
        this.floatEpsilon = floatEpsilon;
    }
}
