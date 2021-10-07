package com.github.leeonky.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;

public class NumberUtil {
    private static final List<Class<?>> NUMBER_TYPES = asList(Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, BigInteger.class, BigDecimal.class);

    public static Class<?> numberType(Class<?> type1, Class<?> type2) {
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
}
