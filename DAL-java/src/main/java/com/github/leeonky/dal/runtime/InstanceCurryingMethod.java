package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.Converter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.leeonky.util.Sneaky.execute;
import static java.util.stream.Collectors.toList;

public class InstanceCurryingMethod {
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

    public static InstanceCurryingMethod createCurryingMethod(Object instance, Method method, Converter converter,
                                                              DALRuntimeContext context) {
        if (Modifier.isStatic(method.getModifiers()))
            return new StaticCurryingMethod(instance, method, converter, context);
        return new InstanceCurryingMethod(instance, method, converter, context);
    }

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

    private boolean testParameterTypes(Predicate<ParameterValue> checking) {
        return method.getParameterCount() - parameterOffset() == parameterValues.size()
                && parameterValues.stream().allMatch(checking);
    }

    public boolean allParamsSameType() {
        return testParameterTypes(ParameterValue::isSameType);
    }

    public boolean allParamsBaseType() {
        return testParameterTypes(ParameterValue::isSuperType);
    }

    public boolean allParamsConvertible() {
        return testParameterTypes(parameterValue -> parameterValue.isConvertibleType(converter));
    }

    public Object resolve() {
        return execute(() -> method.invoke(instance, parameterValues.stream().map(parameterValue ->
                parameterValue.getArg(converter)).collect(toList()).toArray()));
    }

    @Override
    public String toString() {
        return method.toString();
    }

    public boolean isSameInstanceType() {
        return true;
    }
}
