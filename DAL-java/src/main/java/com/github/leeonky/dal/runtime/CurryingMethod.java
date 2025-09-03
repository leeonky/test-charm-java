package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.leeonky.util.Sneaky.execute;
import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class CurryingMethod implements ProxyObject {
    private final List<CandidateMethod> candidateMethods;
    private final DALRuntimeContext runtimeContext;
    private final Data<Object> instance;
    private CandidateMethod resolvedMethod;

    public CurryingMethod(List<CandidateMethod> candidateMethods, DALRuntimeContext runtimeContext, Data<Object> instance) {
        this.candidateMethods = candidateMethods;
        this.runtimeContext = runtimeContext;
        this.instance = instance;
    }

    public CurryingMethod call(Object arg) {
        return new CurryingMethod(candidateMethods.stream().map(curryingMethod ->
                curryingMethod.call(arg)).collect(toList()), runtimeContext, instance);
    }

    public Object resolve() {
        Optional<CandidateMethod> curryingMethod = getFirstPresent(
                () -> selectCurryingMethod(CandidateMethod::allParamsSameType),
                () -> selectCurryingMethod(CandidateMethod::allParamsBaseType),
                () -> selectCurryingMethod(CandidateMethod::allParamsConvertible));
        if (!curryingMethod.isPresent())
            return this;
        Object resolve = curryingMethod.get().resolve(instance.value());
        resolvedMethod = curryingMethod.get();
        return resolve;
    }

    private Optional<CandidateMethod> selectCurryingMethod(Predicate<CandidateMethod> predicate) {
        List<CandidateMethod> methods = candidateMethods.stream().filter(predicate).collect(toList());
        if (methods.size() > 1) {
            List<CandidateMethod> highPriorityMethod = methods.stream().filter(StaticCandidateMethod.class::isInstance).collect(toList());
            return of(getFirstPresent(() -> getOnlyOne(highPriorityMethod),
                    () -> getOnlyOne(highPriorityMethod.stream().filter(curryingMethod -> curryingMethod.isSameInstanceType(instance.value())).collect(toList())))
                    .orElseThrow(() -> new InvalidPropertyException(DumpingBuffer.rootContext(runtimeContext)
                            .append("More than one currying method:").indent(this::dumpCandidates).content())));
        }
        return methods.stream().findFirst();
    }

    private Optional<CandidateMethod> getOnlyOne(List<CandidateMethod> list) {
        if (list.size() == 1)
            return of(list.get(0));
        return Optional.empty();
    }

    @Override
    public Object getValue(Object property) {
        return call(property).resolve();
    }

    public CandidateMethod getResolvedMethod() {
        return resolvedMethod;
    }

    public void dumpCandidates(DumpingBuffer buffer) {
        buffer.newLine().append("instance: ").dumpValue(instance);
        candidateMethods.stream().sorted(comparing(CandidateMethod::toString)).forEach(curryingMethod ->
                buffer.newLine().append(curryingMethod == getResolvedMethod() ? "-> " : "")
                        .append(curryingMethod.toString()).indent(curryingMethod::dumpArguments));
    }

    public static class CandidateMethod {
        protected final Method method;
        protected final DALRuntimeContext context;
        protected final List<CurryingArgument> curryingArguments = new ArrayList<>();

        protected CandidateMethod(Method method, DALRuntimeContext context) {
            this.method = method;
            this.context = context;
        }

        public static CandidateMethod candidateMethod(Method method, DALRuntimeContext context) {
            if (Modifier.isStatic(method.getModifiers()))
                return new StaticCandidateMethod(method, context);
            return new CandidateMethod(method, context);
        }

        public CandidateMethod call(Object arg) {
            CandidateMethod curryingMethod = clone();
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
        protected CandidateMethod clone() {
            return new CandidateMethod(method, context);
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

        public Object resolve(Object instance) {
            return execute(() -> method.invoke(instance,
                    curryingArguments.stream().map(CurryingArgument::properType).toArray()));
        }

        @Override
        public String toString() {
            return method.toString();
        }

        public boolean isSameInstanceType(Object instance) {
            return true;
        }

        public List<CurryingArgument> arguments() {
            return unmodifiableList(curryingArguments);
        }

        public void dumpArguments(DumpingBuffer indentBuffer) {
            arguments().forEach(argument -> indentBuffer.newLine().dumpValue(argument.origin()));
        }
    }
}
