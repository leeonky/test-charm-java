package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;

import static com.github.leeonky.util.BeanClass.getClassName;
import static java.lang.String.format;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

public class Calculator {
    public static int compare(Object v1, Object v2, DALRuntimeContext context) {
        if (v1 == null || v2 == null)
            throw new IllegalArgumentException(format("Can not compare [%s] and [%s]", v1, v2));
        if (v1 instanceof Number && v2 instanceof Number)
            return context.getNumberType().compare((Number) v1, (Number) v2);
        if (v1 instanceof String && v2 instanceof String)
            return ((String) v1).compareTo((String) v2);
        throw new IllegalArgumentException(format("Can not compare [%s: %s] and [%s: %s]",
                getClassName(v1), v1, getClassName(v2), v2));
    }

    public static boolean equals(Data v1, Data v2) {
        if (v1.isNull())
            return v2.isNull();
        return !v2.isNull() && Objects.equals(v1.getInstance(), v2.getInstance());
    }

    public static Object plus(Object v1, Object v2, DALRuntimeContext context) {
        if (v1 instanceof Number && v2 instanceof Number)
            return context.getNumberType().plus((Number) v1, (Number) v2);
        if (v1 instanceof String)
            return v1.toString() + v2;
        if (v2 instanceof String)
            return v1 + v2.toString();
        throw new IllegalArgumentException(format("Can not plus '%s' and '%s'", getClassName(v1), getClassName(v2)));
    }

    public static Object subtract(Object v1, Object v2, DALRuntimeContext context) {
        requireNumber(v1, v2);
        return context.getNumberType().subtract((Number) v1, (Number) v2);
    }

    public static Object multiply(Object v1, Object v2, DALRuntimeContext context) {
        requireNumber(v1, v2);
        return context.getNumberType().multiply((Number) v1, (Number) v2);
    }

    public static Object divide(Object v1, Object v2, DALRuntimeContext context) {
        requireNumber(v1, v2);
        return context.getNumberType().divide((Number) v1, (Number) v2);
    }

    private static void requireNumber(Object v1, Object v2) {
        if (!(v1 instanceof Number && v2 instanceof Number))
            throw new IllegalArgumentException(format("Operands should be number but '%s' and '%s'",
                    getClassName(v1), getClassName(v2)));
    }

    public static Object and(Supplier<Object> s1, Supplier<Object> s2) {
        Object v1 = s1.get();
        requireBooleanType(v1, "Operand 1");
        if ((boolean) v1) {
            Object v2 = s2.get();
            requireBooleanType(v2, "Operand 2");
            return v2;
        }
        return false;
    }

    public static Object or(Supplier<Object> s1, Supplier<Object> s2) {
        Object v1 = s1.get();
        requireBooleanType(v1, "Operand 1");
        if (!(boolean) v1) {
            Object v2 = s2.get();
            requireBooleanType(v2, "Operand 2");
            return v2;
        }
        return true;
    }

    public static Object not(Object v) {
        requireBooleanType(v, "Operand");
        return !(boolean) v;
    }

    public static void requireBooleanType(Object v, final String operand) {
        if (!(v instanceof Boolean))
            throw new IllegalArgumentException(operand + " should be boolean but '" + getClassName(v) + "'");
    }

    @SuppressWarnings("unchecked")
    public static Object negate(Data data, DALRuntimeContext context) {
        Object value = data.getInstance();
        if (value instanceof Number)
            return context.getNumberType().negate((Number) value);
        if (data.isList())
            return data.getValueList().stream().sorted((Comparator) reverseOrder()).collect(toList());
        throw new IllegalArgumentException(format("Operands should be number but '%s'", getClassName(value)));
    }

    @SuppressWarnings("unchecked")
    public static Object positive(Data data, DALRuntimeContext context) {
        Object value = data.getInstance();
        if (value instanceof Number)
            return value;
        if (data.isList())
            return data.getValueList().stream().sorted((Comparator) naturalOrder()).collect(toList());
        throw new IllegalArgumentException(format("Operands should be List but '%s'", getClassName(value)));
    }

    public static boolean less(Object left, Object right, DALRuntimeContext context) {
        return compare(left, right, context) < 0;
    }

    public static boolean greaterOrEqual(Object left, Object right, DALRuntimeContext context) {
        return compare(left, right, context) >= 0;
    }

    public static boolean lessOrEqual(Object left, Object rgiht, DALRuntimeContext context) {
        return compare(left, rgiht, context) <= 0;
    }

    public static boolean greater(Object left, Object right, DALRuntimeContext context) {
        return compare(left, right, context) > 0;
    }

    public static boolean notEqual(Data left, Data right) {
        return !equals(left, right);
    }
}
