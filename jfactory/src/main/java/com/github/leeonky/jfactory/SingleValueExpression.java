package com.github.leeonky.jfactory;

import com.github.leeonky.util.CollectionHelper;
import com.github.leeonky.util.Property;

import java.util.Objects;

class SingleValueExpression<P> extends Expression<P> {
    private final Object value;
    private final TraitsSpec traitsSpec;

    public SingleValueExpression(Object value, TraitsSpec traitsSpec, Property<P> property, boolean forQuery) {
        super(property);
        this.value = value;
        this.traitsSpec = traitsSpec;
    }

    @Override
    protected boolean isPropertyMatch(Object propertyValue) {
        return property.getReaderType().isCollection() ? CollectionHelper.equals(propertyValue, value)
                : Objects.equals(propertyValue, property.getReader().tryConvert(value));
    }

    @Override
    public Producer<?> buildProducer(JFactory jFactory, Producer<P> parent) {
        if (intently)
            return new BuilderValueProducer<>(traitsSpec.toBuilder(jFactory, property.getWriterType()), false);
        return new FixedValueProducer<>(property.getWriterType(), value);
    }
}
