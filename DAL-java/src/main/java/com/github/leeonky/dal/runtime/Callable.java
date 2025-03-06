package com.github.leeonky.dal.runtime;

import java.util.function.Function;

public interface Callable<A, T> extends Function<A, T>, ProxyObject {
    @SuppressWarnings("unchecked")
    @Override
    default Object getValue(Object property) {
        return apply((A) property);
    }
}
