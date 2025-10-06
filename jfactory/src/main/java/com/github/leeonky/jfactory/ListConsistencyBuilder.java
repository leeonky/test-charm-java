package com.github.leeonky.jfactory;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.github.leeonky.util.BeanClass.create;

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
            listConsistency.normalize(c -> aligner.apply(c.convertTo(create(Coordinate.D1.class))),
                    inverseAligner::apply);
            return this;

        }
    }

    public static class D2<T, C extends Coordinate> extends ListConsistencyBuilder<T, C> {
        D2(Consistency<T, C> main, DefaultListConsistency<T, C> listConsistency) {
            super(main, listConsistency);
        }

        public D2<T, C> normalize(Function<Coordinate.D2, C> aligner,
                                  Function<C, Coordinate.D2> inverseAligner) {
            listConsistency.normalize(c -> aligner.apply(c.convertTo(create(Coordinate.D2.class))),
                    inverseAligner::apply);
            return this;
        }
    }
}
