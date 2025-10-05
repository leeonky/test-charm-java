package com.github.leeonky.jfactory;

import java.util.function.Consumer;
import java.util.function.Function;

public class ListConsistencyBuilderA1<T> {
    private final Consistency<T> main;
    private final ListConsistency<T> listConsistency;

    ListConsistencyBuilderA1(Consistency<T> main, ListConsistency<T> listConsistency) {
        this.main = main;
        this.listConsistency = listConsistency;
    }

    public Consistency<T> consistent(Consumer<ListConsistency<T>> definition) {
        definition.accept(listConsistency);
        return main;
    }

    public ListConsistencyBuilderA1<T> normalize(Function<Coordinate, Coordinate> aligner, Function<Coordinate, Coordinate> dealigner) {
        listConsistency.normalize(aligner, dealigner);
        return this;
    }
}
