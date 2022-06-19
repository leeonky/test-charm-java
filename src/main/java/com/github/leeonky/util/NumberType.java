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

    @SuppressWarnings("unchecked")
    public <T extends Number> T convert(Number number, Class<T> type) {
        if (type.isInstance(number))
            return (T) number;
        if ((type.equals(byte.class) || type.equals(Byte.class)) && checkForByte(number))
            return (T) (Byte) number.byteValue();
        if ((type.equals(short.class) || type.equals(Short.class)) && checkForShort(number))
            return (T) (Short) number.shortValue();
        if ((type.equals(int.class) || type.equals(Integer.class)) && checkForInt(number))
            return (T) (Integer) number.intValue();
        throw new IllegalArgumentException(String.format("Cannot convert %s to %s", number, type.getName()));
    }

    private boolean checkForByte(Number number) {
        byte minValue = Byte.MIN_VALUE;
        byte maxValue = Byte.MAX_VALUE;
        if (number instanceof Short)
            return (Short) number >= minValue && (Short) number <= maxValue;
        if (number instanceof Integer)
            return (Integer) number >= minValue && (Integer) number <= maxValue;
        if (number instanceof Long)
            return (Long) number >= minValue && (Long) number <= maxValue;
        if (number instanceof Float)
            return (Float) number >= minValue && (Float) number <= maxValue
                    && Math.round((Float) number) == (Float) number;
        if (number instanceof Double)
            return (Double) number >= minValue && (Double) number <= maxValue
                    && Math.round((Double) number) == (Double) number;
        if (number instanceof BigInteger)
            return ((BigInteger) number).compareTo(BigInteger.valueOf(maxValue)) <= 0
                    && ((BigInteger) number).compareTo(BigInteger.valueOf(minValue)) >= 0;
        if (number instanceof BigDecimal)
            return ((BigDecimal) number).compareTo(BigDecimal.valueOf(maxValue)) <= 0
                    && ((BigDecimal) number).compareTo(BigDecimal.valueOf(minValue)) >= 0
                    && ((BigDecimal) number).stripTrailingZeros().scale() <= 0;
        return true;
    }

    private boolean checkForShort(Number number) {
        short minValue = Short.MIN_VALUE;
        short maxValue = Short.MAX_VALUE;
        if (number instanceof Integer)
            return (Integer) number >= minValue && (Integer) number <= maxValue;
        if (number instanceof Long)
            return (Long) number >= minValue && (Long) number <= maxValue;
        if (number instanceof Float)
            return (Float) number >= minValue && (Float) number <= maxValue
                    && Math.round((Float) number) == (Float) number;
        if (number instanceof Double)
            return (Double) number >= minValue && (Double) number <= maxValue
                    && Math.round((Double) number) == (Double) number;
        if (number instanceof BigInteger)
            return ((BigInteger) number).compareTo(BigInteger.valueOf(maxValue)) <= 0
                    && ((BigInteger) number).compareTo(BigInteger.valueOf(minValue)) >= 0;
        if (number instanceof BigDecimal)
            return ((BigDecimal) number).compareTo(BigDecimal.valueOf(maxValue)) <= 0
                    && ((BigDecimal) number).compareTo(BigDecimal.valueOf(minValue)) >= 0
                    && ((BigDecimal) number).stripTrailingZeros().scale() <= 0;
        return true;
    }

    private boolean checkForInt(Number number) {
        int minValue = Integer.MIN_VALUE;
        int maxValue = Integer.MAX_VALUE;
        if (number instanceof Long)
            return (Long) number >= minValue && (Long) number <= maxValue;
        if (number instanceof Float)
            return (Float) number >= minValue && (Float) number <= maxValue
                    && Math.round((Float) number) == (Float) number;
        if (number instanceof Double)
            return (Double) number >= minValue && (Double) number <= maxValue
                    && Math.round((Double) number) == (Double) number;
        if (number instanceof BigInteger)
            return ((BigInteger) number).compareTo(BigInteger.valueOf(maxValue)) <= 0
                    && ((BigInteger) number).compareTo(BigInteger.valueOf(minValue)) >= 0;
        if (number instanceof BigDecimal)
            return ((BigDecimal) number).compareTo(BigDecimal.valueOf(maxValue)) <= 0
                    && ((BigDecimal) number).compareTo(BigDecimal.valueOf(minValue)) >= 0
                    && ((BigDecimal) number).stripTrailingZeros().scale() <= 0;
        return true;
    }
}
