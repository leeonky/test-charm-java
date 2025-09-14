package com.github.leeonky.jfactory;

import java.util.function.Function;

public interface Consistency<T> {
    Consistency<T> link(ConsistencyItem<T> item);

    void apply(Producer<?> producer);

    Consistency<T> direct(String property);

    <P> P1<T, P> property(String property);

    interface Composer<T> extends Function<Object[], T> {
    }

    interface Decomposer<T> extends Function<T, Object[]> {
    }

    class DecorateConsistency<T> implements Consistency<T> {
        private final Consistency<T> consistency;

        public DecorateConsistency(Consistency<T> consistency) {
            this.consistency = consistency;
        }

        @Override
        public Consistency<T> link(ConsistencyItem<T> item) {
            return consistency.link(item);
        }

        @Override
        public void apply(Producer<?> producer) {
            consistency.apply(producer);
        }

        @Override
        public Consistency<T> direct(String property) {
            return consistency.direct(property);
        }

        @Override
        public <P> P1<T, P> property(String property) {
            return consistency.property(property);
        }
    }

    class P1<T, P> extends DecorateConsistency<T> {
        private final ConsistencyItem<T> lastItem;

        public P1(Consistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin);
            link(this.lastItem = lastItem);
        }

        public P1<T, P> compose(Function<P, T> composer) {
            lastItem.setComposer(objs -> composer.apply((P) objs[0]));
            return this;
        }

        public P1<T, P> decompose(Function<T, P> decomposer) {
            lastItem.setDecomposer(t -> new Object[]{decomposer.apply(t)});
            return this;
        }
    }
}
