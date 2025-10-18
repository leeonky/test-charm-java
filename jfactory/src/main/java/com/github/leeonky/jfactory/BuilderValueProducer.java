package com.github.leeonky.jfactory;

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
        throw new IllegalStateException("Should not produce any value");
    }

    //    TODO full test for merge( queryFirst and !queryFirst, forQuery and !forQuery)
    @Override
    public Producer<T> changeTo(Producer<T> newProducer) {
        if (newProducer instanceof BuilderValueProducer) {
            DefaultBuilder<T> marge = builder.marge(((BuilderValueProducer<T>) newProducer).builder);
            return new BuilderValueProducer<>(marge, true);
        }
//        TODO need test
        return super.changeTo(newProducer);
    }

    @Override
    protected Producer<T> changeFrom(OptionalSpecDefaultValueProducer<T> producer) {
        if (producer.getTraitsAndSpec() != null)
            return new BuilderValueProducer<>(producer.builder().marge(builder), queryFirst);
        return this;
    }

    //    TODO need test missing all test of this method() query in spec and should be created after merge input property
//    TODO forQuery for builder.queryAll()
    @Override
    protected Producer<?> resolveBuilderValueProducer(boolean forQuery) {
        if (!forQuery && queryFirst) {
            T result = builder.query();
            if (result != null)
                return new FixedValueProducer<>(getType(), result);
        }
//        TODO need test
        return builder.createProducer();
    }
}
