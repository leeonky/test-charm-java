package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

class OptionalSpecDefaultValueProducer<V> extends DefaultTypeValueProducer<V> {
    private final String[] traitsAndSpec;

    public OptionalSpecDefaultValueProducer(BeanClass<V> type, String[] traitsAndSpec) {
        super(type);
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
