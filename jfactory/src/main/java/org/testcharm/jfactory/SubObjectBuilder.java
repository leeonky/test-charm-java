package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

import java.util.stream.Collectors;

class SubObjectBuilder extends SubNestedBuilder {
    public SubObjectBuilder(String property, TraitsSpec traitsSpec, boolean force, boolean queryFirst) {
        super(property, queryFirst, force, traitsSpec);
    }

    public SubObjectBuilder(String property, TraitsSpec traitsSpec, boolean force, String substring, Object value, boolean queryFirst) {
        this(property, traitsSpec, force, queryFirst);
        subProperties.put(substring, value);
    }

    @Override
    public Producer<?> buildProducer(Producer<?> parent, ObjectFactory<?> factory, JFactory jFactory) {
        Builder<Object> builder = traitsSpec.toBuilder(jFactory, parent.getType().getProperty(property()).getWriterType()).properties(subProperties);
        return new BuilderValueProducer<>(builder, !force);
    }

    @Override
    protected SubBuilder mergeTo(SubBuilder to) {
        return to.mergeFrom(this);
    }

    @Override
    protected SubBuilder mergeFrom(SubObjectBuilder from) {
        SubObjectBuilder subObjectBuilder = new SubObjectBuilder(property(), traitsSpec.mergeFrom(from.traitsSpec, property()), force || from.force, queryFirst);
        subObjectBuilder.subProperties.putAll(from.subProperties);
        subObjectBuilder.subProperties.putAll(subProperties);
        return subObjectBuilder;
    }

    @Override
    public SubBuilder forceCreate() {
        SubObjectBuilder newBuilder = new SubObjectBuilder(property(), traitsSpec, true, queryFirst);
        newBuilder.subProperties.putAll(subProperties);
        return newBuilder;
    }

    @Override
    public boolean matches(Object object, ObjectFactory<?> objectFactory) {
        if (force)
            return false;
        Object propertyValue = BeanClass.createFrom(object).getPropertyValue(object, property());
        KeyValueCollection.Matcher2 objectMatcher2 = new KeyValueCollection.Matcher2<>(subBuilders(objectFactory).collect(Collectors.toList()));
        return objectMatcher2.matches(propertyValue, objectFactory);
    }
}
