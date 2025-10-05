package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Arrays.asList;

public interface Consistency<T> {
    BeanClass<T> type();

    Consistency<T> direct(String property);

    <P> Consistency.C1<T, P> property(String property);

    <P1, P2> Consistency.C2<T, P1, P2> properties(String property1, String property2);

    <P1, P2, P3> Consistency.C3<T, P1, P2, P3> properties(String property1, String property2, String property3);

    Consistency.CN<T> properties(String... properties);

    ListConsistencyBuilderA1<T> list(String property);

    ListConsistencyBuilderA1<T> list(String property1, String property2);

    class C1<T, P> extends DecorateConsistency<T> {
        private final ConsistencyItem<T> lastItem;

        C1(DefaultConsistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin);
            this.lastItem = lastItem;
        }

        @SuppressWarnings("unchecked")
        public C1<T, P> read(Function<P, T> composer) {
            lastItem.setComposer(new ComposerWrapper<>(objs -> composer.apply((P) objs[0]), composer));
            return this;
        }

        public C1<T, P> write(Function<T, P> decomposer) {
            lastItem.setDecomposer(new DecomposerWrapper<>(t -> new Object[]{decomposer.apply(t)}, decomposer));
            return this;
        }
    }

    class C2<T, P1, P2> extends MultiPropertyConsistency<T, C2<T, P1, P2>> {
        C2(Consistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin, lastItem);
        }

        @SuppressWarnings("unchecked")
        public C2<T, P1, P2> read(BiFunction<P1, P2, T> composer) {
            lastItem.setComposer(new ComposerWrapper<>(objs -> composer.apply((P1) objs[0], (P2) objs[1]), composer));
            return this;
        }

        public C2<T, P1, P2> write(Function<T, P1> decompose1, Function<T, P2> decompose2) {
            lastItem.setDecomposer(new DecomposerWrapper<>(
                    t -> new Object[]{decompose1.apply(t), decompose2.apply(t)}, asList(decompose1, decompose2)));
            return this;
        }
    }

    class C3<T, P1, P2, P3> extends MultiPropertyConsistency<T, C3<T, P1, P2, P3>> {
        C3(Consistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin, lastItem);
        }

        @SuppressWarnings("unchecked")
        public C3<T, P1, P2, P3> read(TriFunction<P1, P2, P3, T> composer) {
            lastItem.setComposer(new ComposerWrapper<>(objs -> composer.apply((P1) objs[0], (P2) objs[1], (P3) objs[2]), composer));
            return this;
        }

        public C3<T, P1, P2, P3> write(Function<T, P1> decompose1, Function<T, P2> decompose2, Function<T, P3> decompose3) {
            lastItem.setDecomposer(new DecomposerWrapper<>(
                    t -> new Object[]{decompose1.apply(t), decompose2.apply(t), decompose3.apply(t)},
                    asList(decompose1, decompose2, decompose3)));
            return this;
        }
    }

    class CN<T> extends MultiPropertyConsistency<T, CN<T>> {
        CN(Consistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin, lastItem);
        }
    }
}
