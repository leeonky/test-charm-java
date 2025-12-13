package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.Sneaky;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.leeonky.jfactory.ConsistencyItem.guessCustomerPositionStackTrace;
import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

class DefaultConsistency<T, C extends Coordinate> implements Consistency<T, C> {
    private final List<ConsistencyItem<T>> items = new ArrayList<>();
    private final List<DefaultListConsistency<T, C>> list = new ArrayList<>();
    private final BeanClass<T> type;
    private final BeanClass<C> coordinateType;
    private final List<StackTraceElement> locations = new ArrayList<>();

    static final Function<Object, Object> LINK_COMPOSER = s -> s;
    static final Function<Object, Object> LINK_DECOMPOSER = s -> s;

    DefaultConsistency(Class<T> type, Class<C> cType) {
        this(BeanClass.create(type), BeanClass.create(cType));
    }

    DefaultConsistency(BeanClass<T> type, BeanClass<C> cType) {
        this(type, cType, singletonList(guessCustomerPositionStackTrace()));
    }

    DefaultConsistency(BeanClass<T> type, BeanClass<C> cType, List<StackTraceElement> locations) {
        this.type = type;
        coordinateType = cType;
        this.locations.addAll(locations);
    }

    @Override
    public BeanClass<T> type() {
        return type;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Consistency<T, C> direct(String property) {
        return property(property).read((Function<Object, T>) LINK_COMPOSER).write((Function<T, Object>) LINK_DECOMPOSER);
    }

    @Override
    public <P> C1<T, P, C> property(String property) {
        ConsistencyItem<T> item = new ConsistencyItem<>(singletonList(propertyChain(property)), this);
        items.add(item);
        return new C1<>(this, item);
    }

    @Override
    public <P1, P2> C2<T, P1, P2, C> properties(String property1, String property2) {
        ConsistencyItem<T> item = new ConsistencyItem<>(asList(propertyChain(property1), propertyChain(property2)), this);
        items.add(item);
        return new C2<>(this, item);
    }

    @Override
    public <P1, P2, P3> C3<T, P1, P2, P3, C> properties(String property1, String property2, String property3) {
        ConsistencyItem<T> item = new ConsistencyItem<>(asList(propertyChain(property1), propertyChain(property2), propertyChain(property3)), this);
        items.add(item);
        return new C3<>(this, item);
    }

    @Override
    public CN<T, C> properties(String... properties) {
        ConsistencyItem<T> item = new ConsistencyItem<>(Arrays.stream(properties).map(PropertyChain::propertyChain).collect(toList()), this);
        items.add(item);
        return new CN<>(this, item);
    }

    boolean merge(DefaultConsistency<?, ?> another) {
        if (items.stream().anyMatch(item -> another.items.stream().anyMatch(item::same))) {
            another.items.forEach(item -> items.add(Sneaky.cast(item)));
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("  ").append(type().getName()).append(":");
        for (StackTraceElement location : locations) {
            builder.append("\n    ").append(location.getClassName()).append(".").append(location.getMethodName())
                    .append("(").append(location.getFileName()).append(":").append(location.getLineNumber()).append(")");
        }

        for (ConsistencyItem<T> item : items) {
            builder.append("\n    - ").append(item);
        }
        return builder.toString();
    }

    DefaultConsistency<T, C> absoluteProperty(PropertyChain base) {
        DefaultConsistency<T, C> absolute = new DefaultConsistency<>(type(), coordinateType(), locations);
        items.forEach(item -> absolute.items.add(item.absoluteProperty(base)));
        return absolute;
    }

    Resolver resolver(ObjectProducer<?> root) {
        return new Resolver(root);
    }

    @Override
    public ListConsistencyBuilder.D1<T, C> list(String property) {
        DefaultListConsistency<T, C> listConsistency = new DefaultListConsistency<>(singletonList(property), this);
        list.add(listConsistency);
        return new ListConsistencyBuilder.D1<>(this, listConsistency);
    }

    @Override
    public ListConsistencyBuilder<T, C> list(String property1, String property2) {
        DefaultListConsistency<T, C> listConsistency = new DefaultListConsistency<>(asList(property1, property2), this);
        list.add(listConsistency);
        return new ListConsistencyBuilder<>(this, listConsistency);
    }

    public Stream<DefaultConsistency<T, C>> populateListConsistencies(ObjectProducer<?> producer) {
        if (list.isEmpty())
            return Stream.of(this);
        return list.stream().flatMap(listConsistency -> listConsistency.enumerateIndices(producer).stream())
                .map(this::populateConsistencyAtCoordinate);
    }

    DefaultConsistency<T, C> populateConsistencyAtCoordinate(C coordinate) {
        DefaultConsistency<T, C> newConsistency = new DefaultConsistency<>(type(), coordinateType(), locations);
        for (DefaultListConsistency<T, C> listConsistency : list) {
            listConsistency.toProperty(coordinate).ifPresent(elementProperty -> {
                for (ListConsistencyItem<T> item : listConsistency.items)
                    item.populateConsistency(elementProperty, newConsistency);
            });
        }
        for (ConsistencyItem<T> item : items)
            newConsistency.items.add(item.copy(newConsistency));
        return newConsistency;
    }

    public BeanClass<C> coordinateType() {
        return coordinateType;
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
        private final Set<ConsistencyItem<T>.Resolver> providerCandidates;
        private final Set<ConsistencyItem<T>.Resolver> consumerCandidates;

        Resolver(ObjectProducer<?> root) {
            List<ConsistencyItem<T>.Resolver> itemResolvers = items.stream().map(i -> i.resolver(root, this)).collect(toList());
            providerCandidates = itemResolvers.stream().filter(ConsistencyItem.Resolver::hasComposer).collect(toCollection(LinkedHashSet::new));
            consumerCandidates = itemResolvers.stream().filter(ConsistencyItem.Resolver::hasDecomposer).collect(toCollection(LinkedHashSet::new));
        }

        Set<PropertyChain> resolve(ConsistencyItem<T>.Resolver provider) {
            Set<PropertyChain> resolved = new HashSet<>();
            for (ConsistencyItem<T>.Resolver consumer : consumerCandidates)
                if (consumer != provider)
                    resolved.addAll(consumer.resolve(provider));
            return resolved;
        }

        Optional<ConsistencyItem<T>.Resolver> searchProvider(Predicate<ConsistencyItem<?>.Resolver> condition) {
            return providerCandidates.stream().filter(condition).min(this::onlyComposerFirstOrder);
        }

        ConsistencyItem<T>.Resolver defaultProvider() {
            return providerCandidates.stream().min(this::onlyComposerFirstOrder).get();
        }

        private int onlyComposerFirstOrder(ConsistencyItem<T>.Resolver r1, ConsistencyItem<T>.Resolver r2) {
            if (!r1.hasDecomposer())
                return -1;
            return !r2.hasDecomposer() ? 1 : 0;
        }

        Optional<ConsistencyItem<T>.Resolver> propertyRelated(PropertyChain property) {
            return providerCandidates.stream().filter(p -> p.containsProperty(property)).findFirst();
        }
    }
}

class DecorateConsistency<T, C extends Coordinate> implements Consistency<T, C> {
    private final Consistency<T, C> delegate;

    DecorateConsistency(Consistency<T, C> delegate) {
        this.delegate = delegate;
    }

    @Override
    public BeanClass<T> type() {
        return delegate.type();
    }

    @Override
    public Consistency<T, C> direct(String property) {
        return delegate.direct(property);
    }

    @Override
    public <P> C1<T, P, C> property(String property) {
        return delegate.property(property);
    }

    @Override
    public <P1, P2> C2<T, P1, P2, C> properties(String property1, String property2) {
        return delegate.properties(property1, property2);
    }

    @Override
    public <P1, P2, P3> C3<T, P1, P2, P3, C> properties(String property1, String property2, String property3) {
        return delegate.properties(property1, property2, property3);
    }

    @Override
    public CN<T, C> properties(String... properties) {
        return delegate.properties(properties);
    }

    @Override
    public ListConsistencyBuilder.D1<T, C> list(String property) {
        return delegate.list(property);
    }

    @Override
    public ListConsistencyBuilder<T, C> list(String property1, String property2) {
        return delegate.list(property1, property2);
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

class MultiPropertyConsistency<T, M extends MultiPropertyConsistency<T, M, C>, C extends Coordinate>
        extends DecorateConsistency<T, C> {
    final ConsistencyItem<T> lastItem;

    MultiPropertyConsistency(Consistency<T, C> origin, ConsistencyItem<T> lastItem) {
        super(origin);
        this.lastItem = lastItem;
    }

    @SuppressWarnings("unchecked")
    public M read(Function<Object[], T> composer) {
        lastItem.setComposer(new ComposerWrapper<>(composer, composer));
        return (M) this;
    }

    @SuppressWarnings("unchecked")
    public M write(Function<T, Object[]> decomposer) {
        lastItem.setDecomposer(new DecomposerWrapper<>(decomposer, decomposer));
        return (M) this;
    }
}
