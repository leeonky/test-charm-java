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
import static java.util.Comparator.*;

public class Calculator {
    private static final NumberType numberType = new NumberType();

    private static int compare(Data v1, Data v2, DALRuntimeContext context) {
        Resolved resolved1 = v1.resolved();
        Resolved resolved2 = v2.resolved();
        return getFirstPresent(() -> resolved1.cast(Number.class).flatMap(number1 -> resolved2.cast(Number.class).map(number2 ->
                        context.getNumberType().compare(number1, number2))),
                () -> resolved1.cast(String.class).flatMap(str1 -> resolved2.cast(String.class).map(str1::compareTo)))
                .orElseThrow(() -> illegalOperation(format("Can not compare %s and %s", dump(resolved1), dump(resolved2))));
    }

    private static String dump(Object instance) {
        return instance == null ? "[null]" : String.format("[%s: %s]", getClassName(instance), instance);
    }

    private static String dump(Resolved resolved) {
        Object value = resolved.value();
        return value == null ? "[null]" : String.format("[%s: %s]", getClassName(value), value);
    }

    public static boolean equals(Resolved resolved1, Resolved resolved2) {
        return resolved1.value() == resolved2.value()
                || opt2(resolved2::isNull) && opt1(resolved1::isNull)
                || resolved2.castList().flatMap(l2 -> resolved1.castList().map(l1 ->
                        collect(resolved2, "2").equals(collect(resolved1, "1"))))
                .orElseGet(() -> Objects.equals(resolved1.value(), resolved2.value()));
    }

    private static List<Object> collect(Resolved resolved, String index) {
        try {
            return resolved.list().collect();
        } catch (InfiniteCollectionException ignore) {
            throw illegalOperation("Invalid operation, operand " + index + " is infinite collection");
        }
    }

    public static Data arithmetic(Data v1, DALOperator opt, Data v2, DALRuntimeContext context) {
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
        return input.map(data -> data.castList().map(l -> sortList(l, reverseOrder()))
                .orElseGet(() -> data.cast(Number.class).map(context.getNumberType()::negate)
                        .orElseThrow(() -> illegalOp2(format("Operand should be number or list but '%s'", getClassName(data.value()))))));
    }

    @SuppressWarnings("unchecked")
    private static Object sortList(Data.DataList list, Comparator<?> comparator) {
        return list.sort(comparing(Data::instance, (Comparator<Object>) comparator));
    }

    public static Data positive(Data input, DALRuntimeContext context) {
        return input.map(data -> data.castList().map(l -> sortList(l, naturalOrder()))
                .orElseThrow(() -> illegalOp2(format("Operand should be list but '%s'", getClassName(data.value())))));
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
        return !equals(left.resolved(), right.resolved());
    }
}
