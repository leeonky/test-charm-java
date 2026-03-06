package org.testcharm.jfactory;

class SubObjectBuilder extends SubBuilder {
    private final boolean force;
    private final TraitsSpec traitsSpec;

    public SubObjectBuilder(String property) {
        super(property);
        force = false;
        traitsSpec = null;
    }

    public SubObjectBuilder(String property, boolean force) {
        super(property);
        this.force = force;
        traitsSpec = null;
    }

    public SubObjectBuilder(String property, TraitsSpec traitsSpec) {
        super(property);
        this.traitsSpec = traitsSpec;
        force = false;
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        return new BuilderValueProducer<>(toBuilder(parent, jFactory), !force);
    }

    private Builder<Object> toBuilder(Producer<?> parent, JFactory jFactory) {
        if (traitsSpec != null)
            return jFactory.spec(traitsSpec.traitsSpec());
        return jFactory.type(parent.getType().getProperty(property).getWriterType());
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder subBuilder) {
        return subBuilder.mergeFrom(this);
    }
}
