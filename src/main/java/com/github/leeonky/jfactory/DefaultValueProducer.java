package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.function.Supplier;

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
}
