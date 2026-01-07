package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.function.Supplier;

public interface Instance<T> {
    int getSequence();

    BeanClass<T> type();

    Supplier<T> reference();

    <P> P param(String key);

    <P> P param(String key, P defaultValue);

    Arguments params(String property);

    Arguments params();

    default int collectionSize() {
        return 0;
    }

    default <T> Supplier<T> rotate(T... values) {
        return () -> values[(getSequence() - 1) % values.length];
    }
}
