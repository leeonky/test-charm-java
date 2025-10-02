package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.function.Supplier;

class DefaultValueProducer<V> extends Producer<V> {
    private final Supplier<V> value;

    DefaultValueProducer(BeanClass<V> type, Supplier<V> value) {
        super(type);
        this.value = value;
    }

    @Override
    protected V produce() {
        return value.get();
    }

    @Override
    protected Producer<V> reChangeFrom(Producer<V> producer) {
        return producer.reChangeTo(this);
    }

    @Override
    protected Producer<V> reChangeTo(DefaultValueProducer<V> newProducer) {
        return newProducer;
    }

    @Override
    protected Producer<V> changeFrom(ObjectProducer<V> producer) {
        return producer;
    }

    @Override
    public Producer<?> childForRead(String property) {
        return getChild(property).orElseGet(() -> new ReadOnlyProducer<>(this, property));
    }
}
