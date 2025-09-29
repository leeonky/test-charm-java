package com.github.leeonky.jfactory;

import java.util.function.Consumer;

public interface ListConsistencyBuilder<T> {
    Consistency<T> consistent(Consumer<ListConsistency<T>> definition);
}
