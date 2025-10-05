package com.github.leeonky.jfactory;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.leeonky.jfactory.Coordinate.d1;

public class ListConsistencyBuilder<T, C extends Coordinate> {
    protected final Consistency<T, C> main;
    final DefaultListConsistency<T, C> listConsistency;

    ListConsistencyBuilder(Consistency<T, C> main, DefaultListConsistency<T, C> listConsistency) {
        this.main = main;
        this.listConsistency = listConsistency;
    }

    public Consistency<T, C> consistent(Consumer<ListConsistency<T, C>> definition) {
        definition.accept(listConsistency);
        return main;
    }

    public ListConsistencyBuilder<T, C> normalize(Normalizer<C> normalizer) {
        listConsistency.normalize(normalizer::align, normalizer::deAlign);
        return this;
    }


    public static class D1<T, C extends Coordinate> extends ListConsistencyBuilder<T, C> {
        D1(Consistency<T, C> main, DefaultListConsistency<T, C> listConsistency) {
            super(main, listConsistency);
        }

        public D1<T, C> normalize(Function<Coordinate.D1, C> aligner,
                                  Function<C, Coordinate.D1> inverseAligner) {
            listConsistency.normalize(c -> aligner.apply(d1(c.indexes().get(0))),
                    inverseAligner::apply);
            return this;

        }
    }
}
