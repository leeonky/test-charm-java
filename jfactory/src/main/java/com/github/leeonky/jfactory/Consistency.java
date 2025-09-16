package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public interface Consistency<T> {
    Function<Object[], ?> LINK_COMPOSER = objs -> objs[0];
    Function<?, Object[]> LINK_DECOMPOSER = t -> new Object[]{t};

    Consistency<T> link(ConsistencyItem<T> item);

    void apply(Producer<?> producer);

    BeanClass<T> type();

    @SuppressWarnings("unchecked")
    default Consistency<T> direct(String property) {
        ConsistencyItem<T> item = new ConsistencyItem<>(singletonList(propertyChain(property)), this);
        item.setComposer(new ComposerWrapper(LINK_COMPOSER, LINK_COMPOSER));
        item.setDecomposer(new DecomposerWrapper(LINK_DECOMPOSER, LINK_DECOMPOSER));
        return link(item);
    }

    default <P> C1<T, P> property(String property) {
        return new C1<>(this, new ConsistencyItem<>(singletonList(propertyChain(property)), this));
    }

    default <P1, P2> C2<T, P1, P2> properties(String property1, String property2) {
        return new C2<>(this, new ConsistencyItem<>(asList(propertyChain(property1), propertyChain(property2)), this));
    }

    interface Identity {
        default Object identity() {
            return this;
        }

        default boolean same(Identity another) {
            return another != null && identity() == another.identity();
        }

        StackTraceElement getLocation();
    }

    interface Composer<T> extends Function<Object[], T>, Identity {
    }

    interface Decomposer<T> extends Function<T, Object[]>, Identity {
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
        public BeanClass<T> type() {
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
    }

    class C1<T, P> extends DecorateConsistency<T> {
        private final ConsistencyItem<T> lastItem;

        public C1(Consistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin);
            link(this.lastItem = lastItem);
        }

        @SuppressWarnings("unchecked")
        public C1<T, P> compose(Function<P, T> composer) {
            lastItem.setComposer(new ComposerWrapper<>(objs -> composer.apply((P) objs[0]), composer));
            return this;
        }

        public C1<T, P> decompose(Function<T, P> decomposer) {
            lastItem.setDecomposer(new DecomposerWrapper<>(t -> new Object[]{decomposer.apply(t)}, decomposer));
            return this;
        }
    }

    class MultiPropertyConsistency<T, C extends MultiPropertyConsistency<T, C>> extends DecorateConsistency<T> {
        protected final ConsistencyItem<T> lastItem;

        public MultiPropertyConsistency(Consistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin);
            this.lastItem = lastItem;
            link(lastItem);
        }

        @SuppressWarnings("unchecked")
        public C compose(Function<Object[], T> composer) {
            lastItem.setComposer(new ComposerWrapper<>(composer, composer));
            return (C) this;
        }

        @SuppressWarnings("unchecked")
        public C decompose(Function<T, Object[]> decomposer) {
            lastItem.setDecomposer(new DecomposerWrapper<>(decomposer, decomposer));
            return (C) this;
        }
    }

    class C2<T, P1, P2> extends MultiPropertyConsistency<T, C2<T, P1, P2>> {
        public C2(Consistency<T> origin, ConsistencyItem<T> lastItem) {
            super(origin, lastItem);
        }

        @SuppressWarnings("unchecked")
        public C2<T, P1, P2> compose(BiFunction<P1, P2, T> composer) {
            lastItem.setComposer(new ComposerWrapper<>(objs -> composer.apply((P1) objs[0], (P2) objs[1]), composer));
            return this;
        }

        public C2<T, P1, P2> decompose(Function<T, P1> decompose1, Function<T, P2> decompose2) {
            lastItem.setDecomposer(new DecomposerWrapper<>(
                    t -> new Object[]{decompose1.apply(t), decompose2.apply(t)}, asList(decompose1, decompose2)));
            return this;
        }
    }
}

class IdentityAction implements Consistency.Identity {
    protected final Object identity;
    protected StackTraceElement location;

    public IdentityAction(Object identity) {
        this.identity = Objects.requireNonNull(identity);
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        location = stackTrace[3];
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

class ComposerWrapper<T> extends IdentityAction implements Consistency.Composer<T> {
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

class DecomposerWrapper<T> extends IdentityAction implements Consistency.Decomposer<T> {
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
