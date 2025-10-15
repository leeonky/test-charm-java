package com.github.leeonky.jfactory;

import com.github.leeonky.util.Property;

class SubObjectExpression<P> extends Expression<P> {
    private final KeyValueCollection properties;
    private final TraitsSpec traitsSpec;
    private final ObjectFactory objectFactory;
    private final Producer<?> subProducer;
    private final boolean forQuery;

    public SubObjectExpression(KeyValueCollection properties, TraitsSpec traitsSpec, Property<P> property,
                               ObjectFactory<?> objectFactory, Producer<?> subProducer, boolean forQuery) {
        super(property);
        this.properties = properties;
        this.traitsSpec = traitsSpec;
        this.objectFactory = objectFactory;
        this.subProducer = subProducer;
        this.forQuery = forQuery;
    }

    @Override
    protected boolean isPropertyMatch(Object propertyValue) {
        return properties.matcher(property.getReaderType(), objectFactory, subProducer).matches(propertyValue);
    }

    @Override
    public Producer<?> buildProducer(JFactory jFactory, Producer<P> parent) {
        return new BuilderValueProducer<>(properties.apply(traitsSpec.toBuilder(jFactory, property.getWriterType())),
                !(intently || forQuery));
    }

    @Override
    public Expression<P> mergeTo(Expression<P> newExpression) {
        return newExpression.mergeFrom(this);
    }

    @Override
    protected Expression<P> mergeFrom(SubObjectExpression<P> origin) {
        properties.insertAll(origin.properties);
        traitsSpec.merge(origin.traitsSpec, property.toString());
        setIntently(intently || origin.intently);
        return this;
    }
}
