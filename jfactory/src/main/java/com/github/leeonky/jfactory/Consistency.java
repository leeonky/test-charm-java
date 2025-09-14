package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.github.leeonky.util.Sneaky.cast;
import static com.github.leeonky.util.Zipped.zip;
import static com.github.leeonky.util.function.Extension.not;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class Consistency<T> {
    private static final List<Class<?>> TYPE_PRIORITY = asList(
            FixedValueProducer.class,
            ReadOnlyProducer.class,
            DependencyProducer.class,
            UnFixedValueProducer.class
    );

    private final List<Item<T>> items = new ArrayList<>();

    public Consistency<T> link(Item<T> item) {
        items.add(item);
        return this;
    }

    void apply(Producer<?> producer) {
        List<Item<T>.Resolving> resolvingList = items.stream().map(i -> i.resolving(producer)).collect(toList());
        Item<T>.Resolving dependency = guessDependency(resolvingList);
        resolvingList.stream().filter(not(dependency::equals))
                .forEach(dependent -> dependent.resolve(dependency));
    }

    private Item<T>.Resolving guessDependency(List<Item<T>.Resolving> resolvingList) {
        for (Class<?> type : TYPE_PRIORITY)
            for (Item<T>.Resolving resolving : resolvingList)
                if (resolving.isProducerType(type))
                    return resolving;
        return resolvingList.get(0);
    }

    public static class Item<T> {
        private final List<PropertyChain> propertyChains;
        private final Function<Object[], T> composer;
        private final Function<T, Object[]> decomposer;

        public Item(List<String> propertyChains, Function<Object[], T> composer, Function<T, Object[]> decomposer, int i1) {
            this.propertyChains = propertyChains.stream().map(PropertyChain::propertyChain).collect(toList());
            this.composer = composer;
            this.decomposer = decomposer;
        }

        public Item(List<PropertyChain> propertyChains, Function<Object[], T> composer, Function<T, Object[]> decomposer) {
            this.propertyChains = propertyChains;
            this.composer = composer;
            this.decomposer = decomposer;
        }

        public Item<T> compose(Function<Object[], T> composer) {
            return new Item<>(propertyChains, composer, decomposer);
        }

        public Item<T> decompose(Function<T, Object[]> decomposer) {
            return new Item<>(propertyChains, composer, decomposer);
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

            private boolean isProducerType(Class<?> type) {
                return type.isInstance(propertyProducers.get(0));
            }

            @SuppressWarnings("unchecked")
            private void resolve(Resolving dependency) {
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

    public static <T> Item<T> direct(String property) {
        return new Item<>(singletonList(property), objs -> (T) objs[0], t -> new Object[]{t}, 0);
    }
}
