package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;

import static com.github.leeonky.jfactory.ConsistencyItem.guessCustomerPositionStackTrace;
import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class DefaultConsistency<T> implements Consistency<T> {
    private static final List<Class<?>> TYPE_PRIORITY = asList(
            FixedValueProducer.class,
            ReadOnlyProducer.class,
            DependencyProducer.class,
            UnFixedValueProducer.class
    );

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

    @Deprecated
    void apply(ObjectProducer<?> producer) {
        Executor executor = executor(producer);
        for (ConsistencyItem<T> item : items)
            item.resolve(producer, executor);

//        List<ConsistencyItem<T>.Resolving> resolvingList = items.stream().map(i -> i.resolving(producer)).collect(toList());
//
//        guessDependency(resolvingList).ifPresent(dependency -> resolvingList.stream().filter(not(dependency::equals))
//                .forEach(dependent -> dependent.resolve(dependency)));
    }

    private Optional<ConsistencyItem<T>.ResolverBk> guessDependency(List<ConsistencyItem<T>.ResolverBk> resolverBkList) {
        List<ConsistencyItem<T>.ResolverBk> candidates = resolverBkList.stream().filter(ConsistencyItem.ResolverBk::hasComposer).collect(toList());
        return getFirstPresent(() -> candidates.stream().filter(ConsistencyItem.ResolverBk::hasFixedProducer).findFirst(),
                () -> {
                    for (Class<?> type : TYPE_PRIORITY)
                        for (ConsistencyItem<T>.ResolverBk resolverBk : candidates)
                            if (resolverBk.isProducerType(type))
                                return of(resolverBk);
                    return candidates.stream().findFirst();
                });
    }

    public boolean merge(DefaultConsistency<?> another) {
        if (items.stream().anyMatch(item -> another.items.stream().anyMatch(item::same))) {
            for (ConsistencyItem item : another.items)
                items.add(item);
            return true;
        }
        return false;
    }

    public boolean dependsOn(DefaultConsistency<?> another) {
        List<ConsistencyItem<?>> dependencyPair = dependencyPair(another);
        if (dependencyPair.isEmpty())
            return false;
        List<ConsistencyItem<?>> reversePair = another.dependencyPair(this);
        if (!reversePair.isEmpty())
            throw new ConflictConsistencyException(format("Conflict dependency between consistencies:\n%s\n%s",
                    dependencyPair.get(0).toTable(dependencyPair.get(1), "  "),
                    reversePair.get(0).toTable(reversePair.get(1), "  ")));
        return true;
    }

    private List<ConsistencyItem<?>> dependencyPair(DefaultConsistency<?> another) {
        for (ConsistencyItem<?> item : items)
            for (ConsistencyItem<?> anotherItem : another.items)
                if (item.dependsOn(anotherItem))
                    return asList(item, anotherItem);
        return Collections.emptyList();
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

    public Executor executor(ObjectProducer<?> rootProducer) {
        return new Executor(rootProducer);
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

        public void resolve(ConsistencyItem<T>.Resolver provider) {
            for (ConsistencyItem<T>.Resolver consumer : consumers) {
                if (consumer != provider) {
                    consumer.extracted(provider);
                }
            }
        }
    }

    @Deprecated
    public class Executor {
        private final ObjectProducer<?> rootProducer;
        private List<ConsistencyItem<T>.ResolverBk> composeResolverBks;

        public Executor(ObjectProducer<?> rootProducer) {
            this.rootProducer = rootProducer;
        }

        T compose() {
            Optional<ConsistencyItem<T>.ResolverBk> optResolving = composeResolvers().stream().findFirst();
            return optResolving.orElseGet(() -> composeResolvers().get(0)).compose();
        }

        private List<ConsistencyItem<T>.ResolverBk> composeResolvers() {
            if (composeResolverBks == null)
                composeResolverBks = items.stream().filter(item -> item.composer != null)
                        .map(item -> item.resolving(rootProducer)).collect(toList());
            return composeResolverBks;
        }

    }
}
