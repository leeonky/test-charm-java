package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.util.NumberType;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static com.github.leeonky.util.Sneaky.execute;
import static java.util.stream.Stream.concat;

class StaticCandidateMethod extends CurryingMethod.CandidateMethod {
    public StaticCandidateMethod(Method method, DALRuntimeContext context) {
        super(method, context);
    }

    @Override
    protected CurryingMethod.CandidateMethod clone() {
        return new StaticCandidateMethod(method, context);
    }

    @Override
    protected int parameterOffset() {
        return 1;
    }

    @Override
    public Object resolve(Object instance) {
        return execute(() -> method.invoke(null, concat(Stream.of(instance),
                curryingArguments.stream().map(CurryingArgument::properType)).toArray()));
    }

    @Override
    public boolean isSameInstanceType(Object instance) {
        return method.getParameters()[0].getType().equals(NumberType.boxedClass(instance.getClass()));
    }
}
