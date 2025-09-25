package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

public class ConsistencyProducer<T, CT> extends Producer<T> {
    private final ConsistencyItem<CT>.Resolver provider;
    private final ConsistencyItem<CT>.Resolver consumer;
    private final int index;

    public ConsistencyProducer(BeanClass<T> type, ConsistencyItem<CT>.Resolver provider,
                               ConsistencyItem<CT>.Resolver consumer, int index) {
        super(type);
        this.provider = provider;
        this.consumer = consumer;
        this.index = index;
    }

    @Override
    protected T produce() {
        return (T) consumer.decompose(provider.compose())[index];
    }
}
