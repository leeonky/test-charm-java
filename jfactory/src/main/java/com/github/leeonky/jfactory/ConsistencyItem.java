package com.github.leeonky.jfactory;

import java.util.List;
import java.util.Objects;

import static com.github.leeonky.util.Sneaky.cast;
import static com.github.leeonky.util.Zipped.zip;
import static java.lang.String.format;
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
            if (!another.consistency.type().equals(consistency.type()))
                throw new ConflictConsistencyException(format("Conflict consistency on property <%s>:%s%s",
                        propertyChains.stream().map(Objects::toString).collect(joining(", ")), this, another));
        }
        return sameProperty;
    }

    @Override
    public String toString() {
        return "\n    " + getPosition()
                + "\n        type: " + consistency.type().getName()
                + "\n        composer: " + composerLocation()
                + "\n        decomposer: " + decomposerLocation();
    }

    private String getPosition() {
        return location.getClassName() + "::" + location.getMethodName() +
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
                beanProducer.changeDescendant(propertyChain, (parent, property) -> new DependencyProducer<>(
                        cast(propertyProducer.getType()),
                        singletonList(dependency::compose),
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
    }
}
