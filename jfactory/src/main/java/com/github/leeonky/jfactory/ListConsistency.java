package com.github.leeonky.jfactory;

import com.github.leeonky.util.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.asList;

public interface ListConsistency<T, C extends Coordinate> {
    ListConsistency<T, C> direct(String property);

    <P> ListConsistency.LC1<T, P, C> property(String property);

    <P1, P2> ListConsistency.LC2<T, P1, P2, C> properties(String property1, String property2);

    <P1, P2, P3> ListConsistency.LC3<T, P1, P2, P3, C> properties(String property1, String property2, String property3);

    class LC1<T, P, C extends Coordinate> extends DecorateListConsistency<T, C> {
        private final ListConsistencyItem<T> lastListConsistencyItem;

        LC1(ListConsistency<T, C> origin, ListConsistencyItem<T> lastListConsistencyItem) {
            super(origin);
            this.lastListConsistencyItem = lastListConsistencyItem;
        }

        public LC1<T, P, C> read(Function<P, T> composer) {
            lastListConsistencyItem.setComposer(new ComposerWrapper<>(objs -> composer.apply((P) objs[0]), composer));
            return this;
        }

        public LC1<T, P, C> write(Function<T, P> decomposer) {
            lastListConsistencyItem.setDecomposer(new DecomposerWrapper<>(t -> new Object[]{decomposer.apply(t)}, decomposer));
            return this;
        }
    }

    class LC2<T, P1, P2, C extends Coordinate> extends MultiPropertyListConsistency<T, LC2<T, P1, P2, C>, C> {
        LC2(ListConsistency<T, C> consistency, ListConsistencyItem<T> listConsistencyItem) {
            super(consistency, listConsistencyItem);
        }

        public LC2<T, P1, P2, C> read(BiFunction<P1, P2, T> composer) {
            last.setComposer(new ComposerWrapper<>(objs -> composer.apply((P1) objs[0], (P2) objs[1]), composer));
            return this;
        }

        public LC2<T, P1, P2, C> write(Function<T, P1> decompose1, Function<T, P2> decompose2) {
            last.setDecomposer(new DecomposerWrapper<>(
                    t -> new Object[]{decompose1.apply(t), decompose2.apply(t)}, asList(decompose1, decompose2)));
            return this;
        }
    }

    class LC3<T, P1, P2, P3, C extends Coordinate> extends MultiPropertyListConsistency<T, LC3<T, P1, P2, P3, C>, C> {
        LC3(ListConsistency<T, C> consistency, ListConsistencyItem<T> listConsistencyItem) {
            super(consistency, listConsistencyItem);
        }

        public LC3<T, P1, P2, P3, C> read(TriFunction<P1, P2, P3, T> composer) {
            last.setComposer(new ComposerWrapper<>(objs -> composer.apply((P1) objs[0], (P2) objs[1], (P3) objs[2]), composer));
            return this;
        }

        public LC3<T, P1, P2, P3, C> write(Function<T, P1> decompose1, Function<T, P2> decompose2, Function<T, P3> decompose3) {
            last.setDecomposer(new DecomposerWrapper<>(
                    t -> new Object[]{decompose1.apply(t), decompose2.apply(t), decompose3.apply(t)},
                    asList(decompose1, decompose2, decompose3)));
            return this;
        }
    }
}
