package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.util.NumberType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.leeonky.util.Sneaky.execute;
import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

public class CurryingMethod implements ProxyObject {
    private final List<CandidateMethod> candidateMethods;
    private final DALRuntimeContext runtimeContext;
    private final Data<Object> instance;
    private CandidateMethod resolvedMethod;

    public CurryingMethod(DALRuntimeContext runtimeContext, Data<Object> instance) {
        candidateMethods = new ArrayList<>();
        this.runtimeContext = runtimeContext;
        this.instance = instance;
    }

    public CurryingMethod call(Object arg) {
        CurryingMethod curryingMethod = new CurryingMethod(runtimeContext, instance);
        candidateMethods.forEach(candidateMethod -> curryingMethod.candidateMethods.add(candidateMethod.call(arg)));
        return curryingMethod;
    }

    public Object resolve() {
        Optional<CandidateMethod> methodOptional = getFirstPresent(
                () -> selectCurryingMethod(CandidateMethod::allParamsSameType),
                () -> selectCurryingMethod(CandidateMethod::allParamsBaseType),
                () -> selectCurryingMethod(CandidateMethod::allParamsConvertible));
        return methodOptional.isPresent() ? (resolvedMethod = methodOptional.get()).resolve() : this;
    }

    private Optional<CandidateMethod> selectCurryingMethod(Predicate<CandidateMethod> predicate) {
        List<CandidateMethod> methods = candidateMethods.stream().filter(predicate).collect(toList());
        if (methods.size() > 1) {
            List<CandidateMethod> highPriorityMethod = methods.stream().filter(StaticCandidateMethod.class::isInstance).collect(toList());
            return of(getFirstPresent(() -> getOnlyOne(highPriorityMethod),
                    () -> getOnlyOne(highPriorityMethod.stream().filter(CandidateMethod::isSameInstanceType).collect(toList())))
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

    public void candidateMethod(Method method) {
        candidateMethods.add(Modifier.isStatic(method.getModifiers()) ?
                new StaticCandidateMethod(method) : new CandidateMethod(method));
    }

    public boolean isEmpty() {
        return candidateMethods.isEmpty();
    }

    public class CandidateMethod {
        protected final Method method;
        protected final List<CurryingArgument> curryingArguments = new ArrayList<>();

        protected CandidateMethod(Method method) {
            this.method = method;
        }

        public CandidateMethod call(Object arg) {
            CandidateMethod candidateMethod = newInstance();
            candidateMethod.curryingArguments.addAll(curryingArguments);
            candidateMethod.curryingArguments.add(new CurryingArgument(currentPositionParameter(), runtimeContext.data(arg)));
            return candidateMethod;
        }

        private Parameter currentPositionParameter() {
            return method.getParameters()[curryingArguments.size() + parameterOffset()];
        }

        protected int parameterOffset() {
            return 0;
        }

        protected CandidateMethod newInstance() {
            return new CandidateMethod(method);
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
            return execute(() -> method.invoke(instance.value(),
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

    class StaticCandidateMethod extends CandidateMethod {
        public StaticCandidateMethod(Method method) {
            super(method);
        }

        @Override
        protected CandidateMethod newInstance() {
            return new StaticCandidateMethod(method);
        }

        @Override
        protected int parameterOffset() {
            return 1;
        }

        @Override
        public Object resolve() {
            return execute(() -> method.invoke(null, concat(Stream.of(instance.value()),
                    curryingArguments.stream().map(CurryingArgument::properType)).toArray()));
        }

        @Override
        public boolean isSameInstanceType() {
            return method.getParameters()[0].getType().equals(NumberType.boxedClass(instance.value().getClass()));
        }
    }
}
