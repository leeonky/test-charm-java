package com.github.leeonky.jfactory;

import java.util.List;

import static com.github.leeonky.util.Sneaky.cast;
import static com.github.leeonky.util.Zipped.zip;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class ConsistencyItem<T> {
    private final List<PropertyChain> propertyChains;
    private DefaultConsistency.Composer<T> composer;
    private DefaultConsistency.Decomposer<T> decomposer;

    public ConsistencyItem(List<PropertyChain> propertyChains, DefaultConsistency.Composer<T> composer, DefaultConsistency.Decomposer<T> decomposer) {
        this.propertyChains = propertyChains;
        this.composer = composer;
        this.decomposer = decomposer;
    }

    public ConsistencyItem(List<PropertyChain> propertyChains) {
        this.propertyChains = propertyChains;
    }

    public void setComposer(DefaultConsistency.Composer<T> composer) {
        this.composer = composer;
    }

    public void setDecomposer(DefaultConsistency.Decomposer<T> decomposer) {
        this.decomposer = decomposer;
    }

    Resolving resolving(Producer<?> producer) {
        return new Resolving(producer);
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
                        singletonList(() -> composer.apply(dependency.valuesForComposing())),
                        values -> decompose((T) values[0])[index]));
            });
        }

        private Object[] decompose(T obj) {
            if (decomposedCachedValues == null)
                decomposedCachedValues = decomposer.apply(obj);
            return decomposedCachedValues;
        }

        private Object[] valuesForComposing() {
            if (cachedValuesForComposing == null)
                cachedValuesForComposing = propertyProducers.stream().map(Producer::getValue).toArray();
            return cachedValuesForComposing;
        }
    }
}
