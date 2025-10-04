package com.github.leeonky.jfactory;

import java.util.function.Consumer;

@Deprecated
public class NestedListConsistencyBuilder<T> {

    private final ListConsistency<T> main;
    private final ListConsistency<T> nested;

    NestedListConsistencyBuilder(ListConsistency<T> main, ListConsistency<T> nested) {
        this.main = main;
        this.nested = nested;
    }

    public ListConsistency<T> consistent(Consumer<ListConsistency<T>> definition) {
        definition.accept(nested);
        return main;
    }
}
