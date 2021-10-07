package com.github.leeonky.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;

public class NumberUtil {
    private static final List<Class<?>> NUMBER_TYPES = asList(Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, BigInteger.class, BigDecimal.class);
    private static final Converter converter = Converter.createDefault();

    public static Class<?> calculationType(Class<?> type1, Class<?> type2) {
        Class<?> boxedType1 = boxedClass(type1);
        Class<?> boxedType2 = boxedClass(type2);
        if (isFloatAndBigInteger(boxedType1, boxedType2) || isFloatAndBigInteger(boxedType2, boxedType1))
            return BigDecimal.class;
        return NUMBER_TYPES.indexOf(boxedType1) > NUMBER_TYPES.indexOf(boxedType2) ? boxedType1 : boxedType2;
    }

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

    private static boolean isFloatAndBigInteger(Class<?> boxedType1, Class<?> boxedType2) {
        return boxedType1.equals(BigInteger.class) && (boxedType2.equals(Float.class) || boxedType2.equals(Double.class));
    }

    public static Number plus(Number left, Number right) {
        Class<?> type = calculationType(left.getClass(), right.getClass());
        Number leftInSameType = (Number) converter.tryConvert(type, left);
        Number rightInSameType = (Number) converter.tryConvert(type, right);
        return plus(leftInSameType, rightInSameType, type);
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
}
