package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.github.leeonky.util.Sneaky.cast;
import static com.github.leeonky.util.Zipped.zip;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class ConsistencyItem<T> {
    private final List<PropertyChain> propertyChains;
    private final Consistency<T> consistency;
    private StackTraceElement location, composerLocation, decomposerLocation;
    private Consistency.Composer<T> composer;
    private Consistency.Decomposer<T> decomposer;

    public ConsistencyItem(List<PropertyChain> propertyChains, Consistency<T> consistency) {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        location = stackTrace[2];
        this.propertyChains = propertyChains;
        this.consistency = consistency;
    }

    public ConsistencyItem<T> changeLocation(StackTraceElement location) {
        this.location = location;
        return this;
    }

    public void setComposer(DefaultConsistency.Composer<T> composer) {
        this.composer = composer;
        composerLocation = composer.getLocation();
    }

    public void setDecomposer(DefaultConsistency.Decomposer<T> decomposer) {
        this.decomposer = decomposer;
        decomposerLocation = decomposer.getLocation();
    }

    Resolving resolving(Producer<?> producer) {
        return new Resolving(producer);
    }

    boolean sameProperty(ConsistencyItem<?> another) {
        boolean sameProperty = propertyChains.equals(another.propertyChains);
        if (sameProperty) {
            if (another.consistency.type().equals(consistency.type())) {
                if (notSameComposer(another) && notSameDecomposer(another))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, composer and decomposer mismatch:\n%s",
                            propertyChains.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));

                if (notSameComposer(another))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, composer mismatch:\n%s",
                            propertyChains.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));

                if (notSameDecomposer(another))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, decomposer mismatch:\n%s",
                            propertyChains.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));

                if (composer == null && another.composer != null || decomposer == null && another.decomposer != null)
                    return false;
            } else {
                if (composer == null && another.composer != null || decomposer == null && another.decomposer != null)
                    return false;
                throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, consistency type mismatch:\n%s",
                        propertyChains.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));
            }
        } else {
            if (propertyChains.stream().anyMatch(another.propertyChains::contains)) {
                throw new ConflictConsistencyException(format("Conflict consistency on property <%s> and <%s>, property overlap:\n%s",
                        propertyChains.stream().map(Objects::toString).collect(joining(", ")),
                        another.propertyChains.stream().map(Objects::toString).collect(joining(", ")),
                        toTable(another, "  ")));
            }
        }
        return sameProperty;
    }

    private boolean notSameComposer(ConsistencyItem<?> another) {
        return composer != null && another.composer != null && !composer.same(another.composer);
    }

    private boolean notSameDecomposer(ConsistencyItem<?> another) {
        return decomposer != null && another.decomposer != null && !decomposer.same(another.decomposer);
    }

    private String toTable(ConsistencyItem<?> another, String linePrefix) {
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

    public ConsistencyItem<T> changeComposerLocation(StackTraceElement stackTraceElement) {
        composerLocation = stackTraceElement;
        return this;
    }

    public ConsistencyItem<T> changeDecomposerLocation(StackTraceElement stackTraceElement) {
        decomposerLocation = stackTraceElement;
        return this;
    }

    class Resolving {
        final Producer<?> beanProducer;
        private final List<Producer<?>> propertyProducers;
        private Object[] decomposedCachedValues;
        private Object[] cachedValuesForComposing;

        Resolving(Producer<?> beanProducer) {
            this.beanProducer = beanProducer;
            propertyProducers = propertyChains.stream().map(beanProducer::descendant).collect(toList());
        }

        boolean isProducerType(Class<?> type) {
            return type.isInstance(propertyProducers.get(0));
        }

        @SuppressWarnings("unchecked")
        void resolve(Resolving dependency) {
            zip(propertyChains, propertyProducers).forEachElementWithIndex((index, propertyChain, propertyProducer) -> {
                if (decomposer != null)
                    beanProducer.changeDescendant(propertyChain, (parent, property) -> new DependencyProducer<>(
                            cast(propertyProducer.getType()), singletonList(dependency::compose),
                            values -> decompose((T) values[0])[index]));
            });
        }

        private T compose() {
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
    }
}
