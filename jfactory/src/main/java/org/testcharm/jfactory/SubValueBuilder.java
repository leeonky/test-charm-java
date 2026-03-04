package org.testcharm.jfactory;

class SubValueBuilder extends SubBuilder {
    private final Object value;

    public SubValueBuilder(String property, Object value) {
        super(property);
        this.value = value;
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder subBuilder) {
        return subBuilder.mergeFrom(this);
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory) {
        return new FixedValueProducer<>(parent.getType().getProperty(property).getWriterType(), factory.transform(property, value));
    }
}
