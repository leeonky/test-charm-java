package com.github.leeonky.jfactory;

import com.github.leeonky.util.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.asList;

public interface ListConsistency<T> {
    ListConsistency<T> direct(String property);

    <P> ListConsistency.LC1<T, P> property(String property);

    <P1, P2> ListConsistency.LC2<T, P1, P2> properties(String property1, String property2);

    <P1, P2, P3> ListConsistency.LC3<T, P1, P2, P3> properties(String property1, String property2, String property3);

    class LC1<T, P> extends DecorateListConsistency<T> {
        private final ListConsistencyItem<T> lastListConsistencyItem;

        LC1(ListConsistency<T> origin, ListConsistencyItem<T> lastListConsistencyItem) {
            super(origin);
            this.lastListConsistencyItem = lastListConsistencyItem;
        }

        public LC1<T, P> read(Function<P, T> composer) {
            lastListConsistencyItem.setComposer(new ComposerWrapper<>(objs -> composer.apply((P) objs[0]), composer));
            return this;
        }

        public LC1<T, P> write(Function<T, P> decomposer) {
            lastListConsistencyItem.setDecomposer(new DecomposerWrapper<>(t -> new Object[]{decomposer.apply(t)}, decomposer));
            return this;
        }
    }

    class LC2<T, P1, P2> extends MultiPropertyListConsistency<T, LC2<T, P1, P2>> {
        LC2(ListConsistency<T> consistency, ListConsistencyItem<T> listConsistencyItem) {
            super(consistency, listConsistencyItem);
        }

        public LC2<T, P1, P2> read(BiFunction<P1, P2, T> composer) {
            last.setComposer(new ComposerWrapper<>(objs -> composer.apply((P1) objs[0], (P2) objs[1]), composer));
            return this;
        }

        public LC2<T, P1, P2> write(Function<T, P1> decompose1, Function<T, P2> decompose2) {
            last.setDecomposer(new DecomposerWrapper<>(
                    t -> new Object[]{decompose1.apply(t), decompose2.apply(t)}, asList(decompose1, decompose2)));
            return this;
        }
    }

    class LC3<T, P1, P2, P3> extends MultiPropertyListConsistency<T, LC3<T, P1, P2, P3>> {
        LC3(ListConsistency<T> consistency, ListConsistencyItem<T> listConsistencyItem) {
            super(consistency, listConsistencyItem);
        }

        public LC3<T, P1, P2, P3> read(TriFunction<P1, P2, P3, T> composer) {
            last.setComposer(new ComposerWrapper<>(objs -> composer.apply((P1) objs[0], (P2) objs[1], (P3) objs[2]), composer));
            return this;
        }

        public LC3<T, P1, P2, P3> write(Function<T, P1> decompose1, Function<T, P2> decompose2, Function<T, P3> decompose3) {
            last.setDecomposer(new DecomposerWrapper<>(
                    t -> new Object[]{decompose1.apply(t), decompose2.apply(t), decompose3.apply(t)},
                    asList(decompose1, decompose2, decompose3)));
            return this;
        }
    }
}
