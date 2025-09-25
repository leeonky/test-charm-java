package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

public class ConsistencyProducer<T, CT> extends Producer<T> {
    private final ConsistencyItem<CT>.Resolver resolver;
    private final ConsistencyItem<CT>.Resolver item;

    public ConsistencyProducer(BeanClass<T> type, ConsistencyItem<CT>.Resolver resolver, ConsistencyItem<CT>.Resolver item) {
        super(type);
        this.resolver = resolver;
        this.item = item;
    }

    @Override
    protected T produce() {
        return (T) item.decompose(resolver.compose())[0];
    }
}
