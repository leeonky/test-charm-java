package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.Converter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.github.leeonky.util.Suppressor.get;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class InstanceCurryingMethod implements CurryingMethod {
    protected final Object instance;
    protected final Method method;
    protected final Converter converter;
    protected final DALRuntimeContext context;
    protected final List<ParameterValue> parameterValues = new ArrayList<>();

    protected InstanceCurryingMethod(Object instance, Method method, Converter converter, DALRuntimeContext context) {
        this.method = method;
        this.instance = instance;
        this.converter = converter;
        this.context = context;
    }

    @Override
    public InstanceCurryingMethod call(Object arg) {
        InstanceCurryingMethod curryingMethod = clone();
        curryingMethod.parameterValues.addAll(parameterValues);
        curryingMethod.parameterValues.add(new ParameterValue(currentPositionParameter(), arg));
        return curryingMethod;
    }

    private Parameter currentPositionParameter() {
        return method.getParameters()[parameterValues.size() + parameterOffset()];
    }

    protected int parameterOffset() {
        return 0;
    }

    @Override
    protected InstanceCurryingMethod clone() {
        return new InstanceCurryingMethod(instance, method, converter, context);
    }

    private String parameterInfo() {
        List<String> parameters = Arrays.stream(method.getParameters()).map(Parameter::toString).collect(toList());
        int argPosition = parameterValues.size() + parameterOffset();
        if (!parameters.isEmpty())
            parameters.set(argPosition, "> " + parameters.get(argPosition));
        return parameters.stream().collect(joining(",\n", format("%s.%s(\n", method.getDeclaringClass().getName(),
                method.getName()), "\n)"));
    }

    public boolean allParamsSameType() {
        return testParameterTypes(ParameterValue::isSameType);
    }

    private boolean testParameterTypes(Predicate<ParameterValue> checking) {
        return isArgCountEnough() && parameterValues.stream().allMatch(checking);
    }

    private boolean isArgCountEnough() {
        return method.getParameterCount() - parameterOffset() == parameterValues.size();
    }

    public boolean allParamsBaseType() {
        return testParameterTypes(ParameterValue::isSuperType);
    }

    public boolean allParamsConvertible() {
        return testParameterTypes(parameterValue -> parameterValue.isConvertibleType(converter));
    }

    @Override
    public Object resolve() {
        return get(() -> method.invoke(instance, parameterValues.stream().map(parameterValue ->
                parameterValue.getArg(converter)).collect(toList()).toArray()));
    }

    @Override
    public Set<Object> fetchArgRange() {
        BiFunction<Object, List<Object>, List<Object>> rangeFactory = context.fetchCurryingMethodArgRange(method);
        if (rangeFactory != null)
            return new LinkedHashSet<>(rangeFactory.apply(instance, parameterValues.stream()
                    .map(parameterValue -> parameterValue.getArg(converter)).collect(toList())));
        System.err.printf("No arg range for %s, give the range or use `:`%n", parameterInfo());
        return emptySet();
    }

    @Override
    public String toString() {
        return method.toString();
    }

    public boolean isSameInstanceType() {
        return true;
    }

    @Override
    public Object convertToArgType(Object obj) {
        return converter.convert(currentPositionParameter().getType(), obj);
    }
}
