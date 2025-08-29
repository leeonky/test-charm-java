package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.util.Converter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.leeonky.util.Sneaky.execute;
import static java.util.Collections.unmodifiableList;

public class InstanceCurryingMethod {
    protected final Object instance;
    protected final Method method;
    protected final Converter converter = null;
    protected final DALRuntimeContext context;
    protected final List<CurryingArgument> curryingArguments = new ArrayList<>();

    protected InstanceCurryingMethod(Object instance, Method method, DALRuntimeContext context) {
        this.method = method;
        this.instance = instance;
        this.context = context;
    }

    public static InstanceCurryingMethod createCurryingMethod(Object instance, Method method,
                                                              DALRuntimeContext context) {
        if (Modifier.isStatic(method.getModifiers()))
            return new StaticCurryingMethod(instance, method, context);
        return new InstanceCurryingMethod(instance, method, context);
    }

    public InstanceCurryingMethod call(Object arg) {
        InstanceCurryingMethod curryingMethod = clone();
        curryingMethod.curryingArguments.addAll(curryingArguments);
        curryingMethod.curryingArguments.add(new CurryingArgument(currentPositionParameter(), context.data(arg)));
        return curryingMethod;
    }

    private Parameter currentPositionParameter() {
        return method.getParameters()[curryingArguments.size() + parameterOffset()];
    }

    protected int parameterOffset() {
        return 0;
    }

    @Override
    protected InstanceCurryingMethod clone() {
        return new InstanceCurryingMethod(instance, method, context);
    }

    private boolean testParameterTypes(Predicate<CurryingArgument> checking) {
        return method.getParameterCount() - parameterOffset() == curryingArguments.size()
                && curryingArguments.stream().allMatch(checking);
    }

    public boolean allParamsSameType() {
        return testParameterTypes(CurryingArgument::isSameType);
    }

    public boolean allParamsBaseType() {
        return testParameterTypes(CurryingArgument::isSuperType);
    }

    public boolean allParamsConvertible() {
        return testParameterTypes(CurryingArgument::isConvertibleType);
    }

    public Object resolve() {
        return execute(() -> method.invoke(instance,
                curryingArguments.stream().map(CurryingArgument::properType).toArray()));
    }

    @Override
    public String toString() {
        return method.toString();
    }

    public boolean isSameInstanceType() {
        return true;
    }

    public List<CurryingArgument> arguments() {
        return unmodifiableList(curryingArguments);
    }

    public void dumpArguments(DumpingBuffer indentBuffer) {
        arguments().forEach(argument -> indentBuffer.newLine().dumpValue(argument.origin()));
    }
}
