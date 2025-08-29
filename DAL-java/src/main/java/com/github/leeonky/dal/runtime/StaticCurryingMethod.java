package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.NumberType;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static com.github.leeonky.util.Sneaky.execute;
import static java.util.stream.Stream.concat;

class StaticCurryingMethod extends InstanceCurryingMethod {
    public StaticCurryingMethod(Object instance, Method method, DALRuntimeContext context) {
        super(instance, method, context);
    }

    @Override
    protected InstanceCurryingMethod clone() {
        return new StaticCurryingMethod(instance, method, context);
    }

    @Override
    protected int parameterOffset() {
        return 1;
    }

    @Override
    public Object resolve() {
        return execute(() -> method.invoke(null, concat(Stream.of(instance),
                curryingArguments.stream().map(CurryingArgument::properType)).toArray()));
    }

    @Override
    public boolean isSameInstanceType() {
        return method.getParameters()[0].getType().equals(NumberType.boxedClass(instance.getClass()));
    }
}
