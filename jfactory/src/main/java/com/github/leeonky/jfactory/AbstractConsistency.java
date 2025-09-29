package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.function.TriFunction;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.leeonky.jfactory.ConsistencyItem.guessCustomerPositionStackTrace;
import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static com.sun.jmx.mbeanserver.Util.cast;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

abstract class AbstractConsistency<T> implements Consistency<T> {
    static final Function<Object[], ?> LINK_COMPOSER = objs -> objs[0];
    static final Function<?, Object[]> LINK_DECOMPOSER = t -> new Object[]{t};

    abstract Consistency<T> link(ConsistencyItem<T> item);

    abstract BeanClass<T> type();

    @Override
    public Consistency<T> direct(String property) {
        ConsistencyItem<T> item = new ConsistencyItem<>(singletonList(propertyChain(property)), this);
        item.setComposer(cast(new ComposerWrapper<>(LINK_COMPOSER, LINK_COMPOSER)));
        item.setDecomposer(cast(new DecomposerWrapper<>(LINK_DECOMPOSER, LINK_DECOMPOSER)));
        return link(item);
    }

    @Override
    public <P> C1<T, P> property(String property) {
        return new C1<>(this, new ConsistencyItem<>(singletonList(propertyChain(property)), this));
    }

    @Override
    public <P1, P2> C2<T, P1, P2> properties(String property1, String property2) {
        return new C2<>(this, new ConsistencyItem<>(asList(propertyChain(property1), propertyChain(property2)), this));
    }

    @Override
    public <P1, P2, P3> C3<T, P1, P2, P3> properties(String property1, String property2, String property3) {
        return new C3<>(this, new ConsistencyItem<>(asList(propertyChain(property1), propertyChain(property2), propertyChain(property3)), this));
    }

    @Override
    public CN<T> properties(String... properties) {
        return new CN<>(this, new ConsistencyItem<>(Arrays.stream(properties).map(PropertyChain::propertyChain).collect(toList()), this));
    }

    public interface Identity {
        default Object identity() {
            return this;
        }

        default boolean same(Identity another) {
            return another != null && identity() == another.identity();
        }

        StackTraceElement getLocation();

        static boolean isSame(Identity identity1, Identity identity2) {
            return identity1 != null && identity2 != null && identity1.same(identity2);
        }

        static boolean isBothNull(Identity identity1, Identity identity2) {
            return identity1 == null && identity2 == null;
        }
    }

    public interface Composer<T> extends Function<Object[], T>, Identity {
    }

    public interface Decomposer<T> extends Function<T, Object[]>, Identity {
    }

    public static class DecorateConsistency<T> extends AbstractConsistency<T> {
        private final AbstractConsistency<T> consistency;

        DecorateConsistency(AbstractConsistency<T> consistency) {
            this.consistency = consistency;
        }

        @Override
        Consistency<T> link(ConsistencyItem<T> item) {
            return consistency.link(item);
        }

        @Override
        BeanClass<T> type() {
            return consistency.type();
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

        @Override
        public <P1, P2, P3> C3<T, P1, P2, P3> properties(String property1, String property2, String property3) {
            return consistency.properties(property1, property2, property3);
        }

        @Override
        public CN<T> properties(String... properties) {
            return consistency.properties(properties);
        }

        @Override
        public ListConsistencyBuilder<T> list(String property) {
            return consistency.list(property);
        }
    }

    public static class C1<T, P> extends DecorateConsistency<T> {
        private final ConsistencyItem<T> lastItem;

        C1(AbstractConsistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin);
            link(this.lastItem = lastItem);
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

    public static class MultiPropertyConsistency<T, C extends MultiPropertyConsistency<T, C>> extends DecorateConsistency<T> {
        final ConsistencyItem<T> lastItem;

        MultiPropertyConsistency(AbstractConsistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin);
            this.lastItem = lastItem;
            link(lastItem);
        }

        @SuppressWarnings("unchecked")
        public C read(Function<Object[], T> composer) {
            lastItem.setComposer(new ComposerWrapper<>(composer, composer));
            return (C) this;
        }

        @SuppressWarnings("unchecked")
        public C write(Function<T, Object[]> decomposer) {
            lastItem.setDecomposer(new DecomposerWrapper<>(decomposer, decomposer));
            return (C) this;
        }
    }

    public static class C2<T, P1, P2> extends MultiPropertyConsistency<T, C2<T, P1, P2>> {
        C2(AbstractConsistency<T> origin, ConsistencyItem<T> lastItem) {
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

    public static class C3<T, P1, P2, P3> extends MultiPropertyConsistency<T, C3<T, P1, P2, P3>> {
        C3(AbstractConsistency<T> origin, ConsistencyItem<T> lastItem) {
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

    public static class CN<T> extends MultiPropertyConsistency<T, CN<T>> {
        CN(AbstractConsistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin, lastItem);
        }
    }

    public static class LC1<T, P> extends DecorateConsistency<T> {
        private final DefaultListConsistency<T> lastListConsistency;
        private final ListConsistencyItem<T> lastListConsistencyItem;

        LC1(AbstractConsistency<T> origin, DefaultListConsistency<T> lastListConsistency, ListConsistencyItem<T> lastListConsistencyItem) {
            super(origin);
            this.lastListConsistency = lastListConsistency;
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
}

class IdentityAction implements AbstractConsistency.Identity {
    protected final Object identity;
    private final StackTraceElement location;

    public IdentityAction(Object identity) {
        this.identity = Objects.requireNonNull(identity);
        location = guessCustomerPositionStackTrace();
    }

    @Override
    public Object identity() {
        return identity;
    }

    @Override
    public StackTraceElement getLocation() {
        return location;
    }
}

class ComposerWrapper<T> extends IdentityAction implements AbstractConsistency.Composer<T> {
    private final Function<Object[], T> action;

    ComposerWrapper(Function<Object[], T> action, Object identity) {
        super(identity);
        this.action = Objects.requireNonNull(action);
    }

    @Override
    public T apply(Object[] objects) {
        return action.apply(objects);
    }
}

class DecomposerWrapper<T> extends IdentityAction implements AbstractConsistency.Decomposer<T> {
    private final Function<T, Object[]> action;

    DecomposerWrapper(Function<T, Object[]> action, Object identity) {
        super(identity);
        this.action = Objects.requireNonNull(action);
    }

    @Override
    public Object[] apply(T t) {
        return action.apply(t);
    }
}
