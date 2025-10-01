package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.Sneaky;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.leeonky.jfactory.ConsistencyItem.guessCustomerPositionStackTrace;
import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

class DefaultConsistency<T> implements Consistency<T> {
    private final List<ConsistencyItem<T>> items = new ArrayList<>();
    private final List<DefaultListConsistency<T>> list = new ArrayList<>();
    private final BeanClass<T> type;
    private final List<StackTraceElement> locations = new ArrayList<>();

    static final Function<Object, Object> LINK_COMPOSER = s -> s;
    static final Function<Object, Object> LINK_DECOMPOSER = s -> s;

    DefaultConsistency(Class<T> type) {
        this(BeanClass.create(type));
    }

    DefaultConsistency(BeanClass<T> type) {
        this(type, singletonList(guessCustomerPositionStackTrace()));
    }

    DefaultConsistency(BeanClass<T> type, List<StackTraceElement> locations) {
        this.type = type;
        this.locations.addAll(locations);
    }

    @Override
    public BeanClass<T> type() {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Consistency<T> direct(String property) {
        return property(property).read((Function<Object, T>) LINK_COMPOSER).write((Function<T, Object>) LINK_DECOMPOSER);
    }

    @Override
    public <P> C1<T, P> property(String property) {
        ConsistencyItem<T> item = new ConsistencyItem<>(singletonList(propertyChain(property)), this);
        items.add(item);
        return new C1<>(this, item);
    }

    @Override
    public <P1, P2> C2<T, P1, P2> properties(String property1, String property2) {
        ConsistencyItem<T> item = new ConsistencyItem<>(asList(propertyChain(property1), propertyChain(property2)), this);
        items.add(item);
        return new C2<>(this, item);
    }

    @Override
    public <P1, P2, P3> C3<T, P1, P2, P3> properties(String property1, String property2, String property3) {
        ConsistencyItem<T> item = new ConsistencyItem<>(asList(propertyChain(property1), propertyChain(property2), propertyChain(property3)), this);
        items.add(item);
        return new C3<>(this, item);
    }

    @Override
    public CN<T> properties(String... properties) {
        ConsistencyItem<T> item = new ConsistencyItem<>(Arrays.stream(properties).map(PropertyChain::propertyChain).collect(toList()), this);
        items.add(item);
        return new CN<>(this, item);
    }

    boolean merge(DefaultConsistency<?> another) {
        if (items.stream().anyMatch(item -> another.items.stream().anyMatch(item::same))) {
            another.items.forEach(item -> items.add(Sneaky.cast(item)));
            return true;
        }
        return false;
    }

    String info() {
        StringBuilder builder = new StringBuilder();
        builder.append("  ").append(type().getName()).append(":");
        for (StackTraceElement location : locations) {
            builder.append("\n    ").append(location.getClassName()).append(".").append(location.getMethodName())
                    .append("(").append(location.getFileName()).append(":").append(location.getLineNumber()).append(")");
        }
        return builder.toString();
    }

    DefaultConsistency<T> absoluteProperty(PropertyChain base) {
        DefaultConsistency<T> absolute = new DefaultConsistency<>(type(), locations);
        items.forEach(item -> absolute.items.add(item.absoluteProperty(base)));
        return absolute;
    }

    Resolver resolver(ObjectProducer<?> root) {
        return new Resolver(root);
    }

    @Override
    public ListConsistencyBuilder<T> list(String property) {
        DefaultListConsistency<T> listConsistency = new DefaultListConsistency<>(property, this);
        list.add(listConsistency);
        return new ListConsistencyBuilder<>(this, listConsistency);
    }

    DefaultConsistency<T> processListConsistency(ObjectProducer<?> producer) {
        list.forEach(listConsistency -> listConsistency.populateConsistencies(producer, propertyChain("")));
        return this;
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

    class Resolver {
        private final Set<ConsistencyItem<T>.Resolver> providers;
        private final Set<ConsistencyItem<T>.Resolver> consumers;

        Resolver(ObjectProducer<?> root) {
            List<ConsistencyItem<T>.Resolver> itemResolvers = items.stream().map(i -> i.resolver(root, this)).collect(toList());
            providers = itemResolvers.stream().filter(ConsistencyItem.Resolver::hasComposer).collect(toCollection(LinkedHashSet::new));
            consumers = itemResolvers.stream().filter(ConsistencyItem.Resolver::hasDecomposer).collect(toCollection(LinkedHashSet::new));
        }

        Set<PropertyChain> resolve(ConsistencyItem<T>.Resolver provider) {
            Set<PropertyChain> resolved = new HashSet<>();
            for (ConsistencyItem<T>.Resolver consumer : consumers)
                if (consumer != provider)
                    resolved.addAll(consumer.resolve(provider));
            return resolved;
        }

        Optional<ConsistencyItem<T>.Resolver> searchProvider(Predicate<ConsistencyItem<?>.Resolver> condition) {
            return providers.stream().filter(condition).min(this::onlyComposerFirstOrder);
        }

        ConsistencyItem<T>.Resolver defaultProvider() {
            return providers.stream().min(this::onlyComposerFirstOrder).get();
        }

        private int onlyComposerFirstOrder(ConsistencyItem<T>.Resolver r1, ConsistencyItem<T>.Resolver r2) {
            if (!r1.hasDecomposer())
                return -1;
            return !r2.hasDecomposer() ? 1 : 0;
        }

        Optional<ConsistencyItem<T>.Resolver> propertyRelated(PropertyChain property) {
            return providers.stream().filter(p -> p.containsProperty(property)).findFirst();
        }
    }
}

class DecorateConsistency<T> implements Consistency<T> {
    private final Consistency<T> delegate;

    DecorateConsistency(Consistency<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public BeanClass<T> type() {
        return delegate.type();
    }

    @Override
    public Consistency<T> direct(String property) {
        return delegate.direct(property);
    }

    @Override
    public <P> C1<T, P> property(String property) {
        return delegate.property(property);
    }

    @Override
    public <P1, P2> C2<T, P1, P2> properties(String property1, String property2) {
        return delegate.properties(property1, property2);
    }

    @Override
    public <P1, P2, P3> C3<T, P1, P2, P3> properties(String property1, String property2, String property3) {
        return delegate.properties(property1, property2, property3);
    }

    @Override
    public CN<T> properties(String... properties) {
        return delegate.properties(properties);
    }

    @Override
    public ListConsistencyBuilder<T> list(String property) {
        return delegate.list(property);
    }
}

class IdentityAction implements DefaultConsistency.Identity {
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

class ComposerWrapper<T> extends IdentityAction implements DefaultConsistency.Composer<T> {
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

class DecomposerWrapper<T> extends IdentityAction implements DefaultConsistency.Decomposer<T> {
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

class MultiPropertyConsistency<T, C extends MultiPropertyConsistency<T, C>> extends DecorateConsistency<T> {
    final ConsistencyItem<T> lastItem;

    MultiPropertyConsistency(Consistency<T> origin, ConsistencyItem<T> lastItem) {
        super(origin);
        this.lastItem = lastItem;
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
