package com.github.leeonky.jfactory;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class ListConsistencyBuilder<T> {
    protected final Consistency<T> main;
    final DefaultListConsistency<T> listConsistency;

    ListConsistencyBuilder(Consistency<T> main, DefaultListConsistency<T> listConsistency) {
        this.main = main;
        this.listConsistency = listConsistency;
    }

    public Consistency<T> consistent(Consumer<ListConsistency<T>> definition) {
        definition.accept(listConsistency);
        return main;
    }

    public ListConsistencyBuilder<T> normalize(Normalizer normalizer) {
        listConsistency.normalize(normalizer::align, normalizer::deAlign);
        return this;
    }


    public static class D1<T> extends ListConsistencyBuilder<T> {
        D1(Consistency<T> main, DefaultListConsistency<T> listConsistency) {
            super(main, listConsistency);
        }

        public D1<T> normalize(Function<Coordinate.D1, Optional<Coordinate>> aligner,
                               Function<Coordinate, Optional<Coordinate.D1>> inverseAligner) {
//            listConsistency.normalize(aligner, inverseAligner);
            return this;
        }
    }
}
