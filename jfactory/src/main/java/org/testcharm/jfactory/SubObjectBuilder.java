package org.testcharm.jfactory;

import java.util.LinkedHashMap;

class SubObjectBuilder extends SubBuilder {
    private final boolean force;
    private final TraitsSpec traitsSpec;
    private final LinkedHashMap<String, Object> subProperties = new LinkedHashMap<>();

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

    public SubObjectBuilder(String property, TraitsSpec traitsSpec, boolean force) {
        super(property);
        this.force = force;
        this.traitsSpec = traitsSpec;
    }

    public SubObjectBuilder(String property, String clause, Object value) {
        super(property);
        force = false;
        traitsSpec = null;
        subProperties.put(clause, value);
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        return new BuilderValueProducer<>(toBuilder(parent, jFactory), !force);
    }

    private Builder<Object> toBuilder(Producer<?> parent, JFactory jFactory) {
        Builder<Object> builder = traitsSpec != null ? jFactory.spec(traitsSpec.traitsSpec())
                : jFactory.type(parent.getType().getProperty(property).getWriterType());
        return builder.properties(subProperties);
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder subBuilder) {
        return subBuilder.mergeFrom(this);
    }

    @Override
    protected SubBuilder mergeFrom(SubObjectBuilder subValueBuilder) {
//        TODO merge force
//        TODO merge spec
        subProperties.putAll(subValueBuilder.subProperties);
        return this;
    }

    public SubBuilder forceCreate() {
        SubObjectBuilder newBuilder = new SubObjectBuilder(property, traitsSpec, true);
        newBuilder.subProperties.putAll(subProperties);
        return newBuilder;
    }
}
