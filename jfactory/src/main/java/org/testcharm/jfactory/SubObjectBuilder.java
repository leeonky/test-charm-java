package org.testcharm.jfactory;

import java.util.LinkedHashMap;

class SubObjectBuilder extends SubBuilder {
    private boolean force;
    private final TraitsSpec traitsSpec;
    private final LinkedHashMap<String, Object> subProperties = new LinkedHashMap<>();

    public SubObjectBuilder(String property, TraitsSpec traitsSpec, boolean force) {
        super(property);
        this.force = force;
        this.traitsSpec = traitsSpec;
    }

    public SubObjectBuilder(String property, TraitsSpec traitsSpec, boolean force, String substring, Object value) {
        super(property);
        this.force = force;
        this.traitsSpec = traitsSpec;
        subProperties.put(substring, value);
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        return new BuilderValueProducer<>(toBuilder(parent, jFactory), !force);
    }

    private Builder<Object> toBuilder(Producer<?> parent, JFactory jFactory) {
        return traitsSpec.toBuilder(jFactory, parent.getType().getProperty(property).getWriterType()).properties(subProperties);
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder to) {
        return to.mergeFrom(this);
    }

    @Override
    protected SubBuilder mergeFrom(SubObjectBuilder from) {
        traitsSpec.mergeFrom(from.traitsSpec, property);
        force = force || from.force;
        subProperties.putAll(from.subProperties);
        return this;
    }

    public SubBuilder forceCreate() {
        SubObjectBuilder newBuilder = new SubObjectBuilder(property, traitsSpec, true);
        newBuilder.subProperties.putAll(subProperties);
        return newBuilder;
    }
}
