package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.asList;

public interface Consistency<T, C extends Coordinate> {
    BeanClass<T> type();

    Consistency<T, C> direct(String property);

    <P> Consistency.C1<T, P, C> property(String property);

    <P1, P2> Consistency.C2<T, P1, P2, C> properties(String property1, String property2);

    <P1, P2, P3> Consistency.C3<T, P1, P2, P3, C> properties(String property1, String property2, String property3);

    Consistency.CN<T, C> properties(String... properties);

    ListConsistencyBuilder.D1<T, C> list(String property);

    ListConsistencyBuilder<T, C> list(String property1, String property2);

    class C1<T, P, C extends Coordinate> extends DecorateConsistency<T, C> {
        private final ConsistencyItem<T> lastItem;

        C1(DefaultConsistency<T, C> origin, ConsistencyItem<T> lastItem) {
            super(origin);
            this.lastItem = lastItem;
        }

        @SuppressWarnings("unchecked")
        public C1<T, P, C> read(Function<P, T> composer) {
            lastItem.setComposer(new ComposerWrapper<>(objs -> composer.apply((P) objs[0]), composer));
            return this;
        }

        public C1<T, P, C> write(Function<T, P> decomposer) {
            lastItem.setDecomposer(new DecomposerWrapper<>(t -> new Object[]{decomposer.apply(t)}, decomposer));
            return this;
        }
    }

    class C2<T, P1, P2, C extends Coordinate> extends MultiPropertyConsistency<T, C2<T, P1, P2, C>, C> {
        C2(Consistency<T, C> origin, ConsistencyItem<T> lastItem) {
            super(origin, lastItem);
        }

        @SuppressWarnings("unchecked")
        public C2<T, P1, P2, C> read(BiFunction<P1, P2, T> composer) {
            lastItem.setComposer(new ComposerWrapper<>(objs -> composer.apply((P1) objs[0], (P2) objs[1]), composer));
            return this;
        }

        public C2<T, P1, P2, C> write(Function<T, P1> decompose1, Function<T, P2> decompose2) {
            lastItem.setDecomposer(new DecomposerWrapper<>(
                    t -> new Object[]{decompose1.apply(t), decompose2.apply(t)}, asList(decompose1, decompose2)));
            return this;
        }
    }

    class C3<T, P1, P2, P3, C extends Coordinate> extends MultiPropertyConsistency<T, C3<T, P1, P2, P3, C>, C> {
        C3(Consistency<T, C> origin, ConsistencyItem<T> lastItem) {
            super(origin, lastItem);
        }

        @SuppressWarnings("unchecked")
        public C3<T, P1, P2, P3, C> read(TriFunction<P1, P2, P3, T> composer) {
            lastItem.setComposer(new ComposerWrapper<>(objs -> composer.apply((P1) objs[0], (P2) objs[1], (P3) objs[2]), composer));
            return this;
        }

        public C3<T, P1, P2, P3, C> write(Function<T, P1> decompose1, Function<T, P2> decompose2, Function<T, P3> decompose3) {
            lastItem.setDecomposer(new DecomposerWrapper<>(
                    t -> new Object[]{decompose1.apply(t), decompose2.apply(t), decompose3.apply(t)},
                    asList(decompose1, decompose2, decompose3)));
            return this;
        }
    }

    class CN<T, C extends Coordinate> extends MultiPropertyConsistency<T, CN<T, C>, C> {
        CN(Consistency<T, C> origin, ConsistencyItem<T> lastItem) {
            super(origin, lastItem);
        }
    }
}
