package com.github.leeonky.jfactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.github.leeonky.jfactory.Consistency.Identity.*;
import static com.github.leeonky.util.Sneaky.cast;
import static com.github.leeonky.util.Zipped.zip;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

class ConsistencyItem<T> {
    final Set<PropertyChain> properties;
    private final DefaultConsistency<T> consistency;
    private final StackTraceElement location;
    private StackTraceElement composerLocation;
    private StackTraceElement decomposerLocation;
    Consistency.Composer<T> composer;
    Consistency.Decomposer<T> decomposer;

    public ConsistencyItem(Collection<PropertyChain> properties, Consistency<T> consistency) {
        location = guessCustomerPositionStackTrace();
        this.properties = new LinkedHashSet<>(properties);
        this.consistency = (DefaultConsistency<T>) consistency;
    }

    ConsistencyItem(Collection<PropertyChain> properties, Consistency<T> consistency, StackTraceElement location) {
        this.properties = new LinkedHashSet<>(properties);
        this.consistency = (DefaultConsistency<T>) consistency;
        this.location = location;
    }

    static StackTraceElement guessCustomerPositionStackTrace() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        return Arrays.stream(stackTrace).filter(s -> !s.getClassName().startsWith("com.github.leeonky.jfactory"))
                .findFirst().orElse(stackTrace[0]);
    }

    public void setComposer(DefaultConsistency.Composer<T> composer) {
        this.composer = composer;
        composerLocation = composer.getLocation();
    }

    public void setDecomposer(DefaultConsistency.Decomposer<T> decomposer) {
        this.decomposer = decomposer;
        decomposerLocation = decomposer.getLocation();
    }

    ResolverBk resolving(Producer<?> producer) {
        return new ResolverBk(producer);
    }

    public boolean same(ConsistencyItem<?> another) {
        return properties.equals(another.properties) &&
                (isSame(composer, another.composer) && isSame(decomposer, another.decomposer)
                        || isBothNull(composer, another.composer) && isSame(decomposer, another.decomposer)
                        || isSame(composer, another.composer) && isBothNull(decomposer, another.decomposer));
    }

    boolean needMerge(ConsistencyItem<?> another) {
        boolean sameProperty = properties.equals(another.properties);

        if (sameProperty) {
            if (another.consistency.type().equals(consistency.type())) {
                if (isNotSame(composer, another.composer) && isNotSame(decomposer, another.decomposer))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, composer and decomposer mismatch:\n%s",
                            properties.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));

                if (isNotSame(composer, another.composer))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, composer mismatch:\n%s",
                            properties.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));

                if (isNotSame(decomposer, another.decomposer))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, decomposer mismatch:\n%s",
                            properties.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));

                return isSame(composer, another.composer) && isSame(decomposer, another.decomposer)
                        || isBothNull(composer, another.composer) && isSame(decomposer, another.decomposer)
                        || isSame(composer, another.composer) && isBothNull(decomposer, another.decomposer);
            } else {
                if ((composer != null && another.composer != null) || (decomposer != null && another.decomposer != null))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, consistency type mismatch:\n%s",
                            properties.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));
                return false;
            }
        } else {
            if (properties.stream().anyMatch(another.properties::contains)) {
                if ((composer != null && another.composer != null) || (decomposer != null && another.decomposer != null))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s> and <%s>, property overlap:\n%s",
                            properties.stream().map(Objects::toString).collect(joining(", ")),
                            another.properties.stream().map(Objects::toString).collect(joining(", ")),
                            toTable(another, "  ")));
            }
        }
        return false;
    }

    public String toTable(ConsistencyItem<?> another, String linePrefix) {
        List<List<String>> data = new ArrayList<>();
        data.add(asList("", "type", "composer", "decomposer"));
        data.add(asList(getPosition(), consistency.type().getName(), composerLocation(), decomposerLocation()));
        data.add(asList(another.getPosition(), another.consistency.type().getName(), another.composerLocation(), another.decomposerLocation()));
        return formatTable(data, linePrefix);
    }

    private String formatTable(List<List<String>> data, String linePrefix) {
        List<Integer> columnWidths = IntStream.range(0, data.get(0).size()).mapToObj(col ->
                data.stream().mapToInt(d -> d.get(col).length()).max().orElse(0)).collect(toList());
        return data.stream().map(line -> {
            AtomicInteger col = new AtomicInteger(0);
            return line.stream().map(cell -> format("%-" + columnWidths.get(col.getAndIncrement()) + "s", cell)).collect(joining(" | ", linePrefix, " |"));
        }).collect(joining("\n"));
    }

    private String getPosition() {
        return location.getClassName() + "." + location.getMethodName() +
                "(" + location.getFileName() + ":" + location.getLineNumber() + ")";
    }

    private String composerLocation() {
        return composerLocation == null ? "null" :
                "(" + composerLocation.getFileName() + ":" + composerLocation.getLineNumber() + ")";
    }

    private String decomposerLocation() {
        return decomposerLocation == null ? "null" :
                "(" + decomposerLocation.getFileName() + ":" + decomposerLocation.getLineNumber() + ")";
    }

    public boolean dependsOn(ConsistencyItem<?> another) {
        return properties.stream().anyMatch(o -> another.properties.stream().anyMatch(o::contains))
                && composer != null && another.decomposer != null;
    }

    public ConsistencyItem<T> absoluteProperty(PropertyChain base) {
        ConsistencyItem<T> absolute = new ConsistencyItem<>(properties.stream().map(base::concat).collect(toList()), consistency, location);
        absolute.decomposer = decomposer;
        absolute.composer = composer;
        absolute.decomposerLocation = decomposerLocation;
        absolute.composerLocation = composerLocation;
        return absolute;
    }

    @Override
    public String toString() {
        return properties.stream().map(Objects::toString).collect(joining(", ")) +
                " => " + consistency.type().getName() +
                (composer != null ? " with composer" : "") +
                (decomposer != null ? " with decomposer" : "");
    }

    @Deprecated
    public void resolve(ObjectProducer<?> producer, DefaultConsistency<T>.Executor executor) {
        if (decomposer != null) {
            int i = 0;
            for (PropertyChain desProperty : properties) {
                int propertyIndex = i++;
                DecomposerExecutor<T> decomposerExecutor = new DecomposerExecutor<>(decomposer, executor);
                producer.changeDescendant(desProperty, (parent, property) ->
                        new ConsistencyProducerBk<>(cast(producer.descendant(desProperty)), propertyIndex, decomposerExecutor));
            }
        }
    }

    public Resolver resolver(ObjectProducer<?> root, DefaultConsistency<T>.Resolver consistency) {
        return new Resolver(root, consistency);
    }

    class Resolver {
        private final ObjectProducer<?> root;
        final DefaultConsistency<T>.Resolver consistency;

        public Resolver(ObjectProducer<?> root, DefaultConsistency<T>.Resolver consistency) {
            this.root = root;
            this.consistency = consistency;
        }

        public boolean hasTypeOf(Class<?> type) {
            return properties.stream().map(root::descendant).anyMatch(type::isInstance);
        }

        public Set<PropertyChain> resolve() {
            return consistency.resolve(this);
        }

        public T compose() {
            return composer.apply(properties.stream().map(root::descendant).map(Producer::getValue).toArray());
        }

        public Object[] decompose(T compose) {
            return decomposer.apply(compose);
        }

        public boolean hasComposer() {
            return composer != null;
        }

        public boolean hasDecomposer() {
            return decomposer != null;
        }

        public Set<PropertyChain> resolve(Resolver provider) {
            int i = 0;
            for (PropertyChain property : properties) {
                int index = i++;
                root.changeDescendant(property, (producer, s) ->
                        new ConsistencyProducer<>(root.descendant(property), provider, this, index));
            }
            return properties;
        }

        @Override
        public int hashCode() {
            return Objects.hash(properties, composer == null ? null : composer.identity(),
                    decomposer == null ? null : decomposer.identity());
        }

        private ConsistencyItem<T> outer() {
            return ConsistencyItem.this;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ConsistencyItem.Resolver) {
                ConsistencyItem<T> another = ((Resolver) o).outer();
                return properties.equals(another.properties) &&
                        (isSame(composer, another.composer) && isSame(decomposer, another.decomposer)
                                || isBothNull(composer, another.composer) && isSame(decomposer, another.decomposer)
                                || isSame(composer, another.composer) && isBothNull(decomposer, another.decomposer));
            }
            return false;
        }

        public boolean hasReadonly() {
            return properties.stream().map(root::descendant).anyMatch(Producer::isFixed);
        }

        public boolean containsProperty(PropertyChain property) {
            return properties.contains(property);
        }

        public DefaultConsistency<T>.Resolver consistencyResolver() {
            return consistency;
        }
    }

    @Deprecated
    class ResolverBk {
        private final Producer<?> beanProducer;
        final List<Producer<?>> propertyProducers;
        private Object[] decomposedCachedValues;
        private Object[] cachedValuesForComposing;

        ResolverBk(Producer<?> beanProducer) {
            this.beanProducer = beanProducer;
            propertyProducers = properties.stream().map(beanProducer::descendant).collect(toList());
        }

        boolean isProducerType(Class<?> type) {
            return propertyProducers.stream().anyMatch(type::isInstance);
        }

        @SuppressWarnings("unchecked")
        void resolve(ResolverBk dependency) {
            zip(properties, propertyProducers).forEachElementWithIndex((index, propertyChain, propertyProducer) -> {
                if (decomposer != null)
                    beanProducer.changeDescendant(propertyChain, (parent, property) -> new DependencyProducer<>(
                            cast(propertyProducer.getType()), singletonList(dependency::compose),
                            values -> decompose((T) values[0])[index]));
            });
        }

        public T compose() {
            if (cachedValuesForComposing == null)
                cachedValuesForComposing = propertyProducers.stream().map(Producer::getValue).toArray();
            return composer.apply(cachedValuesForComposing);
        }

        private Object[] decompose(T obj) {
            if (decomposedCachedValues == null)
                decomposedCachedValues = decomposer.apply(obj);
            return decomposedCachedValues;
        }

        public boolean hasComposer() {
            return composer != null;
        }

        public boolean hasFixedProducer() {
            return propertyProducers.stream().anyMatch(Producer::isFixed);
        }
    }

}
