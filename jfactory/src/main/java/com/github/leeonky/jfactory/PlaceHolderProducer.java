package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Optional;

import static java.util.Optional.of;

class PlaceHolderProducer extends Producer<Object> {
    static final Producer<?> PLACE_HOLDER = new PlaceHolderProducer();

    public PlaceHolderProducer() {
        super(BeanClass.create(Object.class));
    }

    @Override
    protected Object produce() {
        throw new IllegalStateException("This is a place holder producer, can not produce any value");
    }

    @Override
    public Optional<Producer<?>> getChild(String property) {
        return of(PLACE_HOLDER);
    }
}
