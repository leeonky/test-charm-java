package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;
import java.util.function.Predicate;

import static com.github.leeonky.jfactory.ConsistencyItem.guessCustomerPositionStackTrace;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class DefaultConsistency<T> implements Consistency<T> {

    final List<ConsistencyItem<T>> items = new ArrayList<>();
    private final BeanClass<T> type;
    private final List<StackTraceElement> locations = new ArrayList<>();

    public DefaultConsistency(Class<T> type) {
        this(BeanClass.create(type));
    }

    public DefaultConsistency(BeanClass<T> type) {
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
    public Consistency<T> link(ConsistencyItem<T> item) {
        items.add(item);
        return this;
    }

    public boolean merge(DefaultConsistency<?> another) {
        if (items.stream().anyMatch(item -> another.items.stream().anyMatch(item::same))) {
            for (ConsistencyItem item : another.items)
                items.add(item);
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

    public class Resolver {
        final Set<ConsistencyItem<T>.Resolver> providers;
        private final Set<ConsistencyItem<T>.Resolver> consumers;

        public Resolver(ObjectProducer<?> root) {
            List<ConsistencyItem<T>.Resolver> itemResolvers = items.stream().map(i -> i.resolver(root, this)).collect(toList());
            providers = itemResolvers.stream().filter(ConsistencyItem.Resolver::hasComposer).collect(toCollection(LinkedHashSet::new));
            consumers = itemResolvers.stream().filter(ConsistencyItem.Resolver::hasDecomposer).collect(toCollection(LinkedHashSet::new));
        }

        public Set<PropertyChain> resolve(ConsistencyItem<T>.Resolver provider) {
            Set<PropertyChain> resolved = new HashSet<>();
            for (ConsistencyItem<T>.Resolver consumer : consumers)
                if (consumer != provider)
                    resolved.addAll(consumer.resolve(provider));
            return resolved;
        }

        Optional<ConsistencyItem<T>.Resolver> searchProvider(Predicate<ConsistencyItem<?>.Resolver> condition) {
            return providers.stream().filter(condition).findFirst();
        }

        ConsistencyItem<?>.Resolver defaultProvider() {
            return providers.iterator().next();
        }
    }
}
