package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;
import java.util.function.Predicate;

import static com.github.leeonky.jfactory.ConsistencyItem.guessCustomerPositionStackTrace;
import static com.github.leeonky.util.Sneaky.cast;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

class DefaultConsistency<T> extends AbstractConsistency<T> {
    private final List<ConsistencyItem<T>> items = new ArrayList<>();
    private final List<DefaultListConsistency<T>> list = new ArrayList<>();

    private final BeanClass<T> type;
    private final List<StackTraceElement> locations = new ArrayList<>();

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
    BeanClass<T> type() {
        return type;
    }

    @Override
    Consistency<T> link(ConsistencyItem<T> item) {
        items.add(item);
        return this;
    }

    public boolean merge(DefaultConsistency<?> another) {
        if (items.stream().anyMatch(item -> another.items.stream().anyMatch(item::same))) {
            another.items.forEach(item -> items.add(cast(item)));
            return true;
        }
        return false;
    }

    public String info() {
        StringBuilder builder = new StringBuilder();
        builder.append("  ").append(type().getName()).append(":");
        for (StackTraceElement location : locations) {
            builder.append("\n    ").append(location.getClassName()).append(".").append(location.getMethodName())
                    .append("(").append(location.getFileName()).append(":").append(location.getLineNumber()).append(")");
        }
        return builder.toString();
    }

    public DefaultConsistency<T> absoluteProperty(PropertyChain base) {
        DefaultConsistency<T> absolute = new DefaultConsistency<>(type(), locations);
        items.forEach(item -> absolute.items.add(item.absoluteProperty(base)));
        return absolute;
    }

    public Resolver resolver(ObjectProducer<?> root) {
        return new Resolver(root);
    }

    @Override
    public ListConsistency<T> list(String property) {
        DefaultListConsistency<T> listConsistency = new DefaultListConsistency<>(property, this);
        list.add(listConsistency);
        return listConsistency;
    }

    public DefaultConsistency<T> processListConsistency(ObjectProducer<?> producer) {
        for (DefaultListConsistency<T> listConsistency : list)
            listConsistency.resolveToItems(producer);
        return this;
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
