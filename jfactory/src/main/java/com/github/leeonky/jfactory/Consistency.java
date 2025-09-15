package com.github.leeonky.jfactory;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public interface Consistency<T> {
    Consistency<T> link(ConsistencyItem<T> item);

    void apply(Producer<?> producer);

    @SuppressWarnings("unchecked")
    default Consistency<T> direct(String property) {
        return link(new ConsistencyItem<>(singletonList(propertyChain(property)), objs -> (T) objs[0], t -> new Object[]{t}));
    }

    default <P> C1<T, P> property(String property) {
        return new C1<>(this, new ConsistencyItem<>(singletonList(propertyChain(property))));
    }

    default <P1, P2> C2<T, P1, P2> properties(String property1, String property2) {
        return new C2<>(this, new ConsistencyItem<>(asList(propertyChain(property1), propertyChain(property2))));
    }

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
        public <P> C1<T, P> property(String property) {
            return consistency.property(property);
        }

        @Override
        public <P1, P2> C2<T, P1, P2> properties(String property1, String property2) {
            return consistency.properties(property1, property2);
        }
    }

    class C1<T, P> extends DecorateConsistency<T> {
        private final ConsistencyItem<T> lastItem;

        public C1(Consistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin);
            link(this.lastItem = lastItem);
        }

        public C1<T, P> compose(Function<P, T> composer) {
            lastItem.setComposer(objs -> composer.apply((P) objs[0]));
            return this;
        }

        public C1<T, P> decompose(Function<T, P> decomposer) {
            lastItem.setDecomposer(t -> new Object[]{decomposer.apply(t)});
            return this;
        }
    }

    class C2<T, P1, P2> extends DecorateConsistency<T> {
        private final ConsistencyItem<T> lastItem;

        public C2(Consistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin);
            link(this.lastItem = lastItem);
        }

        @SuppressWarnings("unchecked")
        public C2<T, P1, P2> compose(BiFunction<P1, P2, T> composer) {
            return compose(objs -> composer.apply((P1) objs[0], (P2) objs[1]));
        }

        public C2<T, P1, P2> compose(Composer<T> composer) {
            lastItem.setComposer(composer);
            return this;
        }

        public C2<T, P1, P2> decompose(Function<T, P1> decompose1, Function<T, P2> decompose2) {
            lastItem.setDecomposer(t -> new Object[]{decompose1.apply(t), decompose2.apply(t)});
            return this;
        }

        public C2<T, P1, P2> decompose(Decomposer<T> decomposer) {
            lastItem.setDecomposer(decomposer);
            return this;
        }
    }
}
