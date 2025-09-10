package com.github.leeonky.jfactory;

import com.github.leeonky.util.PropertyWriter;

import static java.util.Collections.emptyList;

public class SubInstance<T> extends RootInstance<T> {
    private final PropertyWriter<?> property;

    public SubInstance(PropertyWriter<?> property, Spec<T> spec, Arguments argument, TypeSequence.Sequence sequence) {
        super(spec, argument, sequence);
        this.property = property;
    }

    String propertyInfo() {
        return String.format("%s#%d", property.getName(), getSequence());
    }

    CollectionInstance<T> inCollection() {
        return new CollectionInstance<>(emptyList(), property, spec, arguments, sequence);
    }

    public PropertyWriter<?> getProperty() {
        return property;
    }
}
