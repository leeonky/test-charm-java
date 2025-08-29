package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class CurryingMethodGroup implements ProxyObject {
    private final List<InstanceCurryingMethod> candidateMethods;
    private final DALRuntimeContext runtimeContext;
    private InstanceCurryingMethod resolvedMethod;

    CurryingMethodGroup(List<InstanceCurryingMethod> candidateMethods, DALRuntimeContext runtimeContext) {
        this.candidateMethods = candidateMethods;
        this.runtimeContext = runtimeContext;
    }

    public CurryingMethodGroup call(Object arg) {
        return new CurryingMethodGroup(candidateMethods.stream().map(curryingMethod ->
                curryingMethod.call(arg)).collect(toList()), runtimeContext);
    }

    public Object resolve() {
        Optional<InstanceCurryingMethod> curryingMethod = getFirstPresent(
                () -> selectCurryingMethod(InstanceCurryingMethod::allParamsSameType),
                () -> selectCurryingMethod(InstanceCurryingMethod::allParamsBaseType),
                () -> selectCurryingMethod(InstanceCurryingMethod::allParamsConvertible));
        if (!curryingMethod.isPresent())
            return this;
        Object resolve = curryingMethod.get().resolve();
        resolvedMethod = curryingMethod.get();
        return resolve;
    }

    private Optional<InstanceCurryingMethod> selectCurryingMethod(Predicate<InstanceCurryingMethod> predicate) {
        List<InstanceCurryingMethod> methods = candidateMethods.stream().filter(predicate).collect(toList());
        if (methods.size() > 1) {
            List<InstanceCurryingMethod> highPriorityMethod = methods.stream().filter(StaticCurryingMethod.class::isInstance).collect(toList());
            return of(getFirstPresent(() -> getOnlyOne(highPriorityMethod),
                    () -> getOnlyOne(highPriorityMethod.stream().filter(InstanceCurryingMethod::isSameInstanceType).collect(toList())))
                    .orElseThrow(() -> new InvalidPropertyException(DumpingBuffer.rootContext(runtimeContext)
                            .append("More than one currying method:").indent(this::dumpCandidates).content())));
        }
        return methods.stream().findFirst();
    }

    private Optional<InstanceCurryingMethod> getOnlyOne(List<InstanceCurryingMethod> list) {
        if (list.size() == 1)
            return of(list.get(0));
        return Optional.empty();
    }

    @Override
    public Object getValue(Object property) {
        return call(property).resolve();
    }

    public InstanceCurryingMethod getResolvedMethod() {
        return resolvedMethod;
    }

    public void dumpCandidates(DumpingBuffer buffer) {
        candidateMethods.forEach(curryingMethod -> buffer.newLine()
                .append(curryingMethod == getResolvedMethod() ? "-> " : "")
                .append(curryingMethod.toString()).indent(curryingMethod::dumpArguments));
    }
}
