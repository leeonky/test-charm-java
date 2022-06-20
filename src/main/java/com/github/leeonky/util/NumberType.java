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
        Number result = null;
        if (type.isInstance(number))
            return (T) number;
        if (type.equals(byte.class) || type.equals(Byte.class))
            result = convertToByte(number, number.byteValue());
        if (type.equals(short.class) || type.equals(Short.class))
            result = convertToShort(number, number.shortValue());
        if (type.equals(int.class) || type.equals(Integer.class))
            result = convertToInt(number, number.intValue());
        if (type.equals(long.class) || type.equals(Long.class))
            result = convertToLong(number, number.longValue());
        if (type.equals(float.class) || type.equals(Float.class))
            result = convertToFloat(number, number.floatValue());
        if (type.equals(double.class) || type.equals(Double.class))
            result = convertToDouble(number, number.doubleValue());
        if (type.equals(BigInteger.class))
            result = convertToBigInteger(number);
        if (result == null)
            throw new IllegalArgumentException(String.format("Cannot convert %s to %s", number, type.getName()));
        return (T) result;
    }

    private BigInteger convertToBigInteger(Number number) {
        if (number instanceof BigInteger)
            return (BigInteger) number;
        if (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || number instanceof Long)
            return BigInteger.valueOf(number.longValue());
        if (number instanceof Float && Float.isFinite(number.floatValue())) {
            BigInteger bigInteger = BigDecimal.valueOf(number.floatValue()).toBigInteger();
            if (bigInteger.floatValue() == number.floatValue())
                return bigInteger;
        }
        if (number instanceof Double && Double.isFinite(number.doubleValue())) {
            BigInteger bigInteger = BigDecimal.valueOf(number.doubleValue()).toBigInteger();
            if (bigInteger.doubleValue() == number.doubleValue())
                return bigInteger;
        }
        if (number instanceof BigDecimal) {
            BigInteger bigInteger = ((BigDecimal) number).toBigInteger();
            if (new BigDecimal(bigInteger).compareTo((BigDecimal) number) == 0)
                return bigInteger;
        }
        return null;
    }

    private Double convertToDouble(Number number, double converted) {
        return (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || (number instanceof Long && number.longValue() == (long) converted)
                || number instanceof Float
                || number instanceof Double
                || (number instanceof BigInteger && Double.isFinite(converted) && BigDecimal.valueOf(converted).compareTo(new BigDecimal(number.toString())) == 0)
                || (number instanceof BigDecimal && Double.isFinite(converted) && BigDecimal.valueOf(converted).compareTo((BigDecimal) number) == 0)
        ) ? converted : null;
    }

    private Float convertToFloat(Number number, float converted) {
        return (number instanceof Byte
                || number instanceof Short
                || (number instanceof Integer && number.intValue() == (int) converted)
                || (number instanceof Long && number.longValue() == (long) converted)
                || number instanceof Float
                || (number instanceof Double && number.doubleValue() == (double) converted)
                || (number instanceof BigInteger && Float.isFinite(converted) && BigDecimal.valueOf(converted).compareTo(new BigDecimal(number.toString())) == 0)
                || (number instanceof BigDecimal && Float.isFinite(converted) && BigDecimal.valueOf(converted).compareTo((BigDecimal) number) == 0)
        ) ? converted : null;
    }

    private Long convertToLong(Number number, long converted) {
        return (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || number instanceof Long
                || (number instanceof Float && number.floatValue() == (float) converted)
                || (number instanceof Double && number.doubleValue() == (double) converted)
                || (number instanceof BigInteger && BigInteger.valueOf(converted).compareTo((BigInteger) number) == 0)
                || (number instanceof BigDecimal && BigDecimal.valueOf(converted).compareTo((BigDecimal) number) == 0))
                ? converted : null;
    }

    private Integer convertToInt(Number number, int converted) {
        return (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || (number instanceof Long && number.longValue() == (long) converted)
                || (number instanceof Float && number.floatValue() == (float) converted)
                || (number instanceof Double && number.doubleValue() == (double) converted)
                || (number instanceof BigInteger && BigInteger.valueOf(converted).compareTo((BigInteger) number) == 0)
                || (number instanceof BigDecimal && BigDecimal.valueOf(converted).compareTo((BigDecimal) number) == 0))
                ? converted : null;
    }

    private Short convertToShort(Number number, short converted) {
        return (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer && number.intValue() == (int) converted
                || number instanceof Long && number.longValue() == (long) converted
                || number instanceof Float && number.floatValue() == (float) converted
                || number instanceof Double && number.doubleValue() == (double) converted
                || number instanceof BigInteger && BigInteger.valueOf(converted).compareTo((BigInteger) number) == 0
                || number instanceof BigDecimal && BigDecimal.valueOf(converted).compareTo((BigDecimal) number) == 0)
                ? converted : null;
    }

    private Byte convertToByte(Number number, byte converted) {
        return (number instanceof Byte
                || number instanceof Short && number.shortValue() == (short) converted
                || number instanceof Integer && number.intValue() == (int) converted
                || number instanceof Long && number.longValue() == (long) converted
                || number instanceof Float && number.floatValue() == (float) converted
                || number instanceof Double && number.doubleValue() == (double) converted
                || number instanceof BigInteger && BigInteger.valueOf(converted).compareTo((BigInteger) number) == 0
                || number instanceof BigDecimal && BigDecimal.valueOf(converted).compareTo((BigDecimal) number) == 0)
                ? converted : null;
    }
}
