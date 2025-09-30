package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.of;

class DefaultValueProducer<V> extends Producer<V> {
    private final Supplier<V> value;

    public DefaultValueProducer(BeanClass<V> type, Supplier<V> value) {
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
    public Optional<Producer<?>> getChild(String property) {
        return of(PlaceHolderProducer.PLACE_HOLDER);
    }
}
