package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

class OptionalSpecDefaultValueProducer<V> extends DefaultValueProducer<V> {
    private final String[] traitsAndSpec;

    public OptionalSpecDefaultValueProducer(BeanClass<V> type, String[] traitsAndSpec) {
        super(type, type::createDefault);
        this.traitsAndSpec = traitsAndSpec;
    }

    @Override
    public Producer<V> changeTo(Producer<V> newProducer) {
        return newProducer.changeFrom(this);
    }

    public String[] getTraitsAndSpec() {
        return traitsAndSpec;
    }
}
