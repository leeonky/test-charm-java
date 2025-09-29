package com.github.leeonky.jfactory;

import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.asList;

public interface ListConsistency<T> {
    ListConsistency<T> direct(String property);

    <P> DefaultListConsistency.LC1<T, P> property(String property);

    <P1, P2> DefaultListConsistency.LC2<T, P1, P2> properties(String property1, String property2);

    class MultiPropertyConsistency<T, C extends MultiPropertyConsistency<T, C>> extends DecorateListConsistency<T> {
        final ListConsistencyItem<T> last;

        MultiPropertyConsistency(ListConsistency<T> delegate, ListConsistencyItem<T> last) {
            super(delegate);
            this.last = last;
        }

        @SuppressWarnings("unchecked")
        public C read(Function<Object[], T> composer) {
            last.setComposer(new ComposerWrapper<>(composer, composer));
            return (C) this;
        }

        @SuppressWarnings("unchecked")
        public C write(Function<T, Object[]> decomposer) {
            last.setDecomposer(new DecomposerWrapper<>(decomposer, decomposer));
            return (C) this;
        }
    }

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

    class LC2<T, P1, P2> extends MultiPropertyConsistency<T, LC2<T, P1, P2>> {
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
}
