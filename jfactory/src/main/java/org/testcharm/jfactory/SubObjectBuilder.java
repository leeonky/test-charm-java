package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

import java.util.LinkedHashMap;
import java.util.Map;

class SubObjectBuilder extends SubBuilder {
    //    TODO final
    private boolean force;
    private final TraitsSpec traitsSpec;
    private final LinkedHashMap<String, Object> subProperties = new LinkedHashMap<>();

    public SubObjectBuilder(String property, TraitsSpec traitsSpec, boolean force, SubCollectionBuilder parentCollectionBuilder) {
        super(property, parentCollectionBuilder);
        this.force = force;
        this.traitsSpec = traitsSpec;
    }

    public SubObjectBuilder(String property, TraitsSpec traitsSpec, boolean force, String substring, Object value, SubCollectionBuilder parentCollectionBuilder) {
        this(property, traitsSpec, force, parentCollectionBuilder);
        subProperties.put(substring, value);
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory, BeanClass<?> collectionSpecElementType) {
        return new BuilderValueProducer<>(toBuilder(parent, jFactory, collectionSpecElementType), !force);
    }

    private Builder<Object> toBuilder(Producer<?> parent, JFactory jFactory, BeanClass<?> collectionSpecElementType) {
        return traitsSpec.toBuilder(jFactory, collectionSpecElementType != null ?
                collectionSpecElementType : parent.getType().getProperty(property()).getWriterType()).properties(subProperties);
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder to) {
        return to.mergeFrom(this);
    }

    @Override
    protected SubBuilder mergeFrom(SubObjectBuilder from) {
        traitsSpec.mergeFrom(from.traitsSpec, property());
        force = force || from.force;
        Map<String, Object> mergedSubProperties = new LinkedHashMap<>();
        mergedSubProperties.putAll(from.subProperties);
        mergedSubProperties.putAll(subProperties);
        subProperties.clear();
        subProperties.putAll(mergedSubProperties);
        return this;
    }

    public SubBuilder forceCreate() {
        SubObjectBuilder newBuilder = new SubObjectBuilder(property(), traitsSpec, true, null);
        newBuilder.subProperties.putAll(subProperties);
        return newBuilder;
    }
}
