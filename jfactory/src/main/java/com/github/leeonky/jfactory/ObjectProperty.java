package com.github.leeonky.jfactory;

import com.github.leeonky.util.PropertyWriter;

import static java.util.Collections.emptyList;

public class ObjectProperty<T> {
    private final PropertyWriter<?> property;
    private final Instance<T> instance;

    ObjectProperty(PropertyWriter<?> property, Instance<T> instance) {
        this.property = property;
        this.instance = instance;
    }

    public String propertyInfo() {
        return String.format("%s#%d", property.getName(), instance.getSequence());
    }

    ObjectCollection<T> asCollection() {
        return new ObjectCollection<>(emptyList(), property, instance);
    }

    public PropertyWriter<?> getProperty() {
        return property;
    }

    public Instance<T> instance() {
        return instance;
    }

    ObjectProperty<T> sub(PropertyWriter<?> property) {
        return new ObjectProperty<>(property, instance);
    }
}
