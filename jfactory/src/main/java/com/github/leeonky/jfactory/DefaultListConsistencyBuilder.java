package com.github.leeonky.jfactory;

import java.util.function.Consumer;

class DefaultListConsistencyBuilder<T> implements ListConsistencyBuilder<T> {
    private final DefaultConsistency<T> main;
    private final DefaultListConsistency<T> listConsistency;

    public DefaultListConsistencyBuilder(String property, DefaultConsistency<T> main) {
        this.main = main;
        listConsistency = new DefaultListConsistency<>(property, main);
        main.link(listConsistency);
    }

    @Override
    public Consistency<T> consistent(Consumer<ListConsistency<T>> definition) {
        definition.accept(listConsistency);
        return main;
    }
}
