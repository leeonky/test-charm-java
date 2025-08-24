package com.github.leeonky.dal.runtime;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class CurryingMethodGroup implements ProxyObject {
    private final List<InstanceCurryingMethod> curryingMethods;

    CurryingMethodGroup(List<InstanceCurryingMethod> curryingMethods) {
        this.curryingMethods = curryingMethods;
    }

    public CurryingMethodGroup call(Object arg) {
        return new CurryingMethodGroup(curryingMethods.stream().map(curryingMethod ->
                curryingMethod.call(arg)).collect(toList()));
    }

    public Object resolve() {
        Optional<InstanceCurryingMethod> curryingMethod = getFirstPresent(
                () -> selectCurryingMethod(InstanceCurryingMethod::allParamsSameType),
                () -> selectCurryingMethod(InstanceCurryingMethod::allParamsBaseType),
                () -> selectCurryingMethod(InstanceCurryingMethod::allParamsConvertible));
        return curryingMethod.isPresent() ? curryingMethod.get().resolve() : this;
    }

    private Optional<InstanceCurryingMethod> selectCurryingMethod(Predicate<InstanceCurryingMethod> predicate) {
        List<InstanceCurryingMethod> methods = curryingMethods.stream().filter(predicate).collect(toList());
        if (methods.size() > 1) {
            List<InstanceCurryingMethod> highPriorityMethod = methods.stream().filter(StaticCurryingMethod.class::isInstance).collect(toList());
            return of(getFirstPresent(() -> getOnlyOne(highPriorityMethod), () -> getOnlyOne(highPriorityMethod.stream()
                    .filter(InstanceCurryingMethod::isSameInstanceType).collect(toList())))
                    .orElseThrow(() -> new InvalidPropertyException("More than one currying method:\n"
                            + methods.stream().map(instanceCurryingMethod -> "  " + instanceCurryingMethod.toString())
                            .sorted().collect(joining("\n")))));
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
}
