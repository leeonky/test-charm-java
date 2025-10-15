package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

public class BuilderValueProducer<T> extends Producer<T> {
    private final DefaultBuilder<T> builder;
    private final boolean queryFirst;

    public BuilderValueProducer(Builder<T> builder, boolean queryFirst) {
        super(builder.getType());
        this.builder = (DefaultBuilder<T>) builder;
        this.queryFirst = queryFirst;
    }

    @Override
    protected T produce() {
        return BeanClass.getConverter().convert(getType().getType(), builder.query());
    }

    @Override
    public Producer<T> changeTo(Producer<T> newProducer) {
        if (newProducer instanceof BuilderValueProducer) {
            if (builder instanceof DefaultBuilder && ((BuilderValueProducer<Object>) newProducer).builder instanceof DefaultBuilder) {
                DefaultBuilder<T> marge = ((DefaultBuilder<T>) builder).marge((DefaultBuilder<T>) ((BuilderValueProducer<Object>) newProducer).builder);
                return new BuilderValueProducer<>(marge, true);
            }
//        TODO need test
            return newProducer;
        }
        if (newProducer instanceof ObjectProducer)
            return builder.marge(((ObjectProducer<T>) newProducer).builder).createProducer();
//        TODO need test
        return this;
    }

    //    TODO need test missing all test of this method() query in spec and should be created after merge input property
    public Producer<?> getProducer() {
        if (queryFirst) {
            T result = builder.query();
            if (result != null)
                return new FixedValueProducer<>(getType(), result);
        }
//        TODO need test
        return builder.createProducer();
    }
}
