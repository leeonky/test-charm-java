package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Optional;

import static java.util.Optional.of;

class DefaultTypeValueProducer<T> extends DefaultValueProducer<T> {
    public DefaultTypeValueProducer(BeanClass<T> type) {
        super(type, type::createDefault);
    }

    @Override
    public Optional<Producer<?>> getChild(String property) {
        return of(PlaceHolderProducer.PLACE_HOLDER);
    }
}
