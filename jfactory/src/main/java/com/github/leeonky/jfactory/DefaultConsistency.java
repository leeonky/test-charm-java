package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static com.github.leeonky.util.function.Extension.not;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class DefaultConsistency<T> implements Consistency<T> {
    private static final List<Class<?>> TYPE_PRIORITY = asList(
            FixedValueProducer.class,
            ReadOnlyProducer.class,
            DependencyProducer.class,
            UnFixedValueProducer.class
    );

    private final List<ConsistencyItem<T>> items = new ArrayList<>();

    @Override
    public Consistency<T> link(ConsistencyItem<T> item) {
        items.add(item);
        return this;
    }

    @Override
    public void apply(Producer<?> producer) {
        List<ConsistencyItem<T>.Resolving> resolvingList = items.stream().map(i -> i.resolving(producer)).collect(toList());
        ConsistencyItem<T>.Resolving dependency = guessDependency(resolvingList);
        resolvingList.stream().filter(not(dependency::equals))
                .forEach(dependent -> dependent.resolve(dependency));
    }

    private ConsistencyItem<T>.Resolving guessDependency(List<ConsistencyItem<T>.Resolving> resolvingList) {
        for (Class<?> type : TYPE_PRIORITY)
            for (ConsistencyItem<T>.Resolving resolving : resolvingList)
                if (resolving.isProducerType(type))
                    return resolving;
        return resolvingList.get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Consistency<T> direct(String property) {
        return link(new ConsistencyItem<>(singletonList(propertyChain(property)), objs -> (T) objs[0], t -> new Object[]{t}));
    }

    @Override
    public <P> Consistency.P1<T, P> property(String property) {
        return new P1<>(this, new ConsistencyItem<>(singletonList(propertyChain(property))));
    }

//    public static class Item1<T, P> extends Item<T> {
//        public Item1(Composer<T> composer, Decomposer<T> decomposer, PropertyChain... propertyChains) {
//            super(composer, decomposer, propertyChains);
//        }
//
//        public Item1<T, P> compose(Function<P, T> composer) {
//            return new Item1<>(objects -> composer.apply((P) objects[0]), decomposer, propertyChains);
//        }
//
//        public Item1<T, P> decompose(Function<T, P> decomposer) {
//            return new Item1<>(composer, t -> new Object[]{decomposer.apply(t)}, propertyChains);
//        }
//    }
}
