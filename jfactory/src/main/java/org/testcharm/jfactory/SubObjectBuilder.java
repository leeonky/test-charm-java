package org.testcharm.jfactory;

class SubObjectBuilder extends SubBuilder {
    private final boolean force;

    public SubObjectBuilder(String property, boolean force) {
        super(property);
        this.force = force;
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        return new BuilderValueProducer<>(jFactory.type(parent.getType().getProperty(property).getWriterType()), !force);
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder subBuilder) {
        return subBuilder.mergeFrom(this);
    }
}
