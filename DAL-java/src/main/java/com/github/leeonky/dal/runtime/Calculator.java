package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.NumberType;
import com.github.leeonky.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.github.leeonky.dal.runtime.Data.ResolvedMethods.cast;
import static com.github.leeonky.dal.runtime.ExpressionException.*;
import static com.github.leeonky.util.Classes.getClassName;
import static com.github.leeonky.util.Pair.pair;
import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.lang.String.format;
import static java.util.Comparator.*;

public class Calculator {
    private static final NumberType numberType = new NumberType();

    private static int compare(Pair<Resolved> pair, DALRuntimeContext context) {
        return getFirstPresent(
                () -> pair.both(cast(Number.class), (num1, num2) -> context.getNumberType().compare(num1, num2)),
                () -> pair.both(cast(String.class), String::compareTo)).orElseThrow(() ->
                illegalOperation(pair.map(Calculator::dump, (s1, s2) -> format("Can not compare %s and %s", s1, s2))));
    }

    private static String dump(Resolved resolved) {
        Object value = resolved.value();
        return value == null ? "[null]" : format("[%s: %s]", getClassName(value), value);
    }

    public static boolean equals(Data data1, Data data2) {
        Resolved resolved1 = data1.resolved();
        Resolved resolved2 = data2.resolved();
        return data1.instance() == data2.instance()
                || opt2(resolved2::isNull) && opt1(resolved1::isNull)
                || resolved2.castList().flatMap(l2 -> resolved1.castList().map(l1 ->
                        collect(data2, "2").equals(collect(data1, "1"))))
                .orElseGet(() -> Objects.equals(resolved1.value(), resolved2.value()));
    }

    private static List<Object> collect(Data data, String index) {
        try {
            return data.list().collect();
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
        if (v instanceof Boolean)
            return !(boolean) v;
        throw illegalOperation("Operand" + " should be boolean but '" + getClassName(v) + "'");
    }

    public static Data negate(Data input, DALRuntimeContext context) {
        return input.map(data -> data.castList().map(l -> sortList(l, reverseOrder(), context))
                .orElseGet(() -> data.cast(Number.class).map(context.getNumberType()::negate)
                        .orElseThrow(() -> illegalOp2(format("Operand should be number or list but '%s'", getClassName(data.value()))))));
    }

    @SuppressWarnings("unchecked")
    private static Object sortList(Data.DataList list, Comparator<?> comparator, DALRuntimeContext context) {
        return list.sort(comparing(data -> context.transformComparable(data.instance()), (Comparator<Object>) comparator));
    }

    public static Data positive(Data input, DALRuntimeContext context) {
        return input.map(data -> data.castList().map(l -> sortList(l, naturalOrder(), context))
                .orElseThrow(() -> illegalOp2(format("Operand should be list but '%s'", getClassName(data.value())))));
    }

    public static Object less(Data left, DALOperator opt, Data right, DALRuntimeContext context) {
        return compare(pair(left.resolved(), right.resolved()), context) < 0;
    }

    public static Object greaterOrEqual(Data left, DALOperator opt, Data right, DALRuntimeContext context) {
        return compare(pair(left.resolved(), right.resolved()), context) >= 0;
    }

    public static Object lessOrEqual(Data left, DALOperator opt, Data right, DALRuntimeContext context) {
        return compare(pair(left.resolved(), right.resolved()), context) <= 0;
    }

    public static Object greater(Data left, DALOperator opt, Data right, DALRuntimeContext context) {
        return compare(pair(left.resolved(), right.resolved()), context) > 0;
    }

    public static boolean notEqual(Data left, Data right) {
        return !equals(left, right);
    }
}
