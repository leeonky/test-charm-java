package com.github.leeonky.jfactory;

import java.util.HashSet;
import java.util.Set;

public class ConsistencyProducer<T, CT> extends Producer<T> {
    private final Producer<T> origin;
    private final ConsistencyItem<CT>.Resolver provider;
    private final ConsistencyItem<CT>.Resolver consumer;
    private final int index;
    private final Set<Producer<?>> stack = new HashSet<>();

    public ConsistencyProducer(Producer<T> origin, ConsistencyItem<CT>.Resolver provider,
                               ConsistencyItem<CT>.Resolver consumer, int index) {
        super(origin.getType());
        this.origin = origin;
        this.provider = provider;
        this.consumer = consumer;
        this.index = index;
    }

    @Override
    protected T produce() {
        if (stack.contains(this))
            return origin.produce();
        stack.add(this);
        return (T) consumer.decompose(provider.compose())[index];
    }

    @Override
    protected Producer<T> changeTo(DefaultValueProducer<T> newProducer) {
        return super.changeTo(newProducer);
    }

    @Override
    protected Producer<T> changeFrom(Producer<T> producer) {
        return super.changeFrom(producer);
    }
}
