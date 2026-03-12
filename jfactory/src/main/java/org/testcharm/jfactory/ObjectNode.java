package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

import java.util.stream.Collectors;

class ObjectNode extends CompositeBuilder {
    public ObjectNode(String property, TraitsSpec traitsSpec, boolean force) {
        super(property, traitsSpec, force);
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        Builder<Object> builder = traitsSpec.toBuilder(jFactory, parent.getType().getProperty(property()).getWriterType())
                .properties(subProperties);
        return new BuilderValueProducer<>(builder, !force);
    }

    @Override
    protected PropertyNode mergeTo(PropertyNode to) {
        return to.mergeFrom(this);
    }

    @Override
    protected PropertyNode mergeFrom(ObjectNode from) {
        ObjectNode objectNode = new ObjectNode(property(), traitsSpec.mergeFrom(from.traitsSpec, property()), force || from.force);
        objectNode.subProperties.putAll(from.subProperties);
        objectNode.subProperties.putAll(subProperties);
        return objectNode;
    }

    @Override
    public PropertyNode forceCreate() {
        ObjectNode newBuilder = new ObjectNode(property(), traitsSpec, true);
        newBuilder.subProperties.putAll(subProperties);
        return newBuilder;
    }

    @Override
    public boolean matches(Object object, ObjectFactory<?> objectFactory) {
        if (force)
            return false;
        Object propertyValue = BeanClass.createFrom(object).getPropertyValue(object, property());
        Matcher objectMatcher = new Matcher<>(createSubNodes(objectFactory).collect(Collectors.toList()));
        return objectMatcher.matches(propertyValue, objectFactory);
    }
}
