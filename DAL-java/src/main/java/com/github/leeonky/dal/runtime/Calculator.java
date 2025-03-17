package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.NumberType;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.github.leeonky.dal.runtime.ExpressionException.*;
import static com.github.leeonky.util.Classes.getClassName;
import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.lang.String.format;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.reverseOrder;

public class Calculator {
    private static final NumberType numberType = new NumberType();

    private static int compare(Data v1, Data v2, DALRuntimeContext context) {
        Object instance1 = v1.instance();
        Object instance2 = v2.instance();
        if (instance1 == null || instance2 == null)
            throw illegalOperation(format("Can not compare [%s] and [%s]", instance1, instance2));
        if (instance1 instanceof Number && instance2 instanceof Number)
            return context.getNumberType().compare((Number) instance1, (Number) instance2);
        if (instance1 instanceof String && instance2 instanceof String)
            return ((String) instance1).compareTo((String) instance2);
        throw illegalOperation(format("Can not compare [%s: %s] and [%s: %s]",
                getClassName(instance1), instance1, getClassName(instance2), instance2));
    }

    public static boolean equals(Data v1, Data v2) {
        if (v1.instance() == v2.instance())
            return true;
        if (opt2(v2.get(Resolved::isNull)))
            return opt1(v1.get(Resolved::isNull));
        if (v2.isList() && v1.isList())
            return collect(v2, "2").equals(collect(v1, "1"));
        return Objects.equals(v1.instance(), v2.instance());
    }

    private static List<Object> collect(Data v2, String index) {
        try {
            return v2.list().collect();
        } catch (InfiniteCollectionException ignore) {
            throw illegalOperation("Invalid operation, operand " + index + " is infinite collection");
        }
    }

    public static Data plus(Data v1, DALOperator opt, Data v2, DALRuntimeContext context) {
        return context.calculate(v1, opt, v2);
    }

    public static Data subtract(Data v1, DALOperator opt, Data v2, DALRuntimeContext context) {
        return context.calculate(v1, opt, v2);
    }

    public static Data multiply(Data v1, DALOperator opt, Data v2, DALRuntimeContext context) {
        return context.calculate(v1, opt, v2);
    }

    public static Data divide(Data v1, DALOperator opt, Data v2, DALRuntimeContext context) {
        return context.calculate(v1, opt, v2);
    }

    public static Data and(Supplier<Data> s1, Supplier<Data> s2) {
        Data v1 = s1.get();
        return isTrue(v1) ? s2.get() : v1;
    }

    private static boolean isTrue(Data value) {
        Resolved resolved = value.resolved();
        return getFirstPresent(() -> resolved.cast(Boolean.class),
                () -> resolved.cast(Number.class).map(number -> numberType.compare(0, number) != 0)
        ).orElseGet(() -> !resolved.isNull());
    }

    public static Data or(Supplier<Data> s1, Supplier<Data> s2) {
        Data v1 = s1.get();
        return isTrue(v1) ? v1 : s2.get();
    }

    public static Object not(Object v) {
        if (!(v instanceof Boolean))
            throw illegalOperation("Operand" + " should be boolean but '" + getClassName(v) + "'");
        return !(boolean) v;
    }

    public static Data negate(Data input, DALRuntimeContext context) {
        return input.map(data -> data.isList() ? sortList(data, reverseOrder())
                : data.cast(Number.class).map(context.getNumberType()::negate).orElseThrow(() ->
                illegalOp2(format("Operand should be number or list but '%s'", getClassName(data.value())))));
    }

    @SuppressWarnings("unchecked")
    private static Data.DataList sortList(Resolved resolved, Comparator<?> comparator) {
        try {
            return resolved.asList().sort(Comparator.comparing(Data::instance, (Comparator<Object>) comparator));
        } catch (InfiniteCollectionException e) {
            throw illegalOperation("Can not sort infinite collection");
        }
    }

    public static Data positive(Data data, DALRuntimeContext context) {
        return data.map(resolved -> {
            if (data.isList())
                return sortList(data.resolved(), naturalOrder());
            throw illegalOp2(format("Operand should be list but '%s'", getClassName(resolved.value())));
        });
    }

    public static Data less(Data left, DALOperator opt, Data right, DALRuntimeContext context) {
        return context.wrap(() -> compare(left, right, context) < 0);
    }

    public static Data greaterOrEqual(Data left, DALOperator opt, Data right, DALRuntimeContext context) {
        return context.wrap(() -> compare(left, right, context) >= 0);
    }

    public static Data lessOrEqual(Data left, DALOperator opt, Data right, DALRuntimeContext context) {
        return context.wrap(() -> compare(left, right, context) <= 0);
    }

    public static Data greater(Data left, DALOperator opt, Data right, DALRuntimeContext context) {
        return context.wrap(() -> compare(left, right, context) > 0);
    }

    public static boolean notEqual(Data left, Data right) {
        return !equals(left, right);
    }
}
