package org.testcharm.jfactory;

class SubObjectBuilder extends SubBuilder {
    public SubObjectBuilder(String property) {
        super(property);
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        return new BuilderValueProducer<>(jFactory.type(parent.getType().getProperty(property).getWriterType()), true);
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder subBuilder) {
        return subBuilder.mergeFrom(this);
    }
}
