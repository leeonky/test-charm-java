package com.github.leeonky.jfactory;

class OptionalSpecDefaultValueProducer<V> extends DefaultTypeValueProducer<V> {
    private final String[] traitsAndSpec;
    final JFactory jFactory;
    private final DefaultBuilder<V> builder;

    //    TODO refactor pass builder instead of traitsAndSpec and jFactory
    public OptionalSpecDefaultValueProducer(String[] traitsAndSpec, JFactory jFactory, Builder<V> builder) {
        super(builder.getType());
        this.traitsAndSpec = traitsAndSpec;
        this.jFactory = jFactory;
        this.builder = (DefaultBuilder<V>) builder;
    }

    @Override
    public Producer<V> changeTo(Producer<V> newProducer) {
        return newProducer.changeFrom(this);
    }

    public String[] getTraitsAndSpec() {
        return traitsAndSpec;
    }

    public DefaultBuilder<V> builder() {
        return builder;
    }
}
