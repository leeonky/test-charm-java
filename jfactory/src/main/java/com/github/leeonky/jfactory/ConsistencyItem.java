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

public class ConsistencyItem<T> {
    private final Set<PropertyChain> propertyChains;
    private final Consistency<T> consistency;
    private final StackTraceElement location;
    private StackTraceElement composerLocation;
    private StackTraceElement decomposerLocation;
    private Consistency.Composer<T> composer;
    private Consistency.Decomposer<T> decomposer;

    public ConsistencyItem(Collection<PropertyChain> propertyChains, Consistency<T> consistency) {
        location = guessCustomerPositionStackTrace();
        this.propertyChains = new LinkedHashSet<>(propertyChains);
        this.consistency = consistency;
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

    Resolving resolving(Producer<?> producer) {
        return new Resolving(producer);
    }

    boolean needMerge(ConsistencyItem<?> another) {
        boolean sameProperty = propertyChains.equals(another.propertyChains);
        if (sameProperty) {
            if (another.consistency.type().equals(consistency.type())) {
                if (isNotSame(composer, another.composer) && isNotSame(decomposer, another.decomposer))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, composer and decomposer mismatch:\n%s",
                            propertyChains.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));

                if (isNotSame(composer, another.composer))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, composer mismatch:\n%s",
                            propertyChains.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));

                if (isNotSame(decomposer, another.decomposer))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, decomposer mismatch:\n%s",
                            propertyChains.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));

                return isSame(composer, another.composer) && isSame(decomposer, another.decomposer)
                        || isBothNull(composer, another.composer) && isSame(decomposer, another.decomposer)
                        || isSame(composer, another.composer) && isBothNull(decomposer, another.decomposer);
            } else {
                if ((composer != null && another.composer != null) || (decomposer != null && another.decomposer != null))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s>, consistency type mismatch:\n%s",
                            propertyChains.stream().map(Objects::toString).collect(joining(", ")), toTable(another, "  ")));
                return false;
            }
        } else {
            if (propertyChains.stream().anyMatch(another.propertyChains::contains)) {
                if ((composer != null && another.composer != null) || (decomposer != null && another.decomposer != null))
                    throw new ConflictConsistencyException(format("Conflict consistency on property <%s> and <%s>, property overlap:\n%s",
                            propertyChains.stream().map(Objects::toString).collect(joining(", ")),
                            another.propertyChains.stream().map(Objects::toString).collect(joining(", ")),
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
        return propertyChains.stream().anyMatch(another.propertyChains::contains)
                && composer != null && another.decomposer != null;
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
            return propertyProducers.stream().anyMatch(type::isInstance);
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
