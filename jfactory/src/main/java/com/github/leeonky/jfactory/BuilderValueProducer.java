package com.github.leeonky.jfactory;

public class BuilderValueProducer<T> extends Producer<T> {
    //    TODO refactor
    final DefaultBuilder<T> builder;
    final boolean queryFirst;

    public BuilderValueProducer(Builder<T> builder, boolean queryFirst) {
        super(builder.getType());
        this.builder = (DefaultBuilder<T>) builder;
        this.queryFirst = queryFirst;
    }

    @Override
    protected T produce() {
        throw new IllegalStateException("Should not produce any value");
    }

    //    TODO full test for merge( queryFirst and !queryFirst, forQuery and !forQuery)
    @Override
    public Producer<T> changeTo(Producer<T> newProducer) {
        if (newProducer instanceof BuilderValueProducer) {
            if (builder instanceof DefaultBuilder && ((BuilderValueProducer<Object>) newProducer).builder instanceof DefaultBuilder) {
                DefaultBuilder<T> marge = builder.marge((DefaultBuilder<T>) ((BuilderValueProducer<Object>) newProducer).builder);
                return new BuilderValueProducer<>(marge, true);
            }
//        TODO need test
            return newProducer;
        }
        if (newProducer instanceof ObjectProducer)
            return builder.marge(((ObjectProducer<T>) newProducer).builder).createProducer();
//        TODO need test
        return super.changeTo(newProducer);
    }

    @Override
    protected Producer<T> changeFrom(OptionalSpecDefaultValueProducer<T> producer) {
        if (producer.getTraitsAndSpec() != null)
            return producer.builder().marge(builder).createProducer();
        return this;
    }

    //    TODO need test missing all test of this method() query in spec and should be created after merge input property
//    TODO forQuery for builder.queryAll()
    @Override
    protected Producer<?> changeToLast(boolean forQuery) {
        if (!forQuery && queryFirst) {
            T result = builder.query();
            if (result != null)
                return new FixedValueProducer<>(getType(), result);
        }
//        TODO need test
        return builder.createProducer();
    }
}
