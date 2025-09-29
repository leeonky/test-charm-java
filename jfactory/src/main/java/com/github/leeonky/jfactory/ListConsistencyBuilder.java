package com.github.leeonky.jfactory;

import java.util.function.Consumer;

public class ListConsistencyBuilder<T> {
    private final Consistency<T> main;
    private final ListConsistency<T> listConsistency;

    ListConsistencyBuilder(Consistency<T> main, ListConsistency<T> listConsistency) {
        this.main = main;
        this.listConsistency = listConsistency;
    }

    public Consistency<T> consistent(Consumer<ListConsistency<T>> definition) {
        definition.accept(listConsistency);
        return main;
    }
}
