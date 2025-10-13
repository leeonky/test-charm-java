package com.github.leeonky.jfactory;

import com.github.leeonky.util.PropertyWriter;

import java.util.ArrayList;
import java.util.List;

class ObjectCollection<T> extends ObjectProperty<T> {
    private final List<Integer> indexes;

    ObjectCollection(List<Integer> indexes, PropertyWriter<?> property, Instance<T> instance) {
        super(property, instance);
        this.indexes = new ArrayList<>(indexes);
    }

    @Override
    ObjectProperty<T> sub(PropertyWriter<?> property) {
        try {
            return new ObjectCollectionElement<>(new ArrayList<Integer>(indexes) {{
                add(Integer.parseInt(property.getName()));
            }}, getProperty(), instance());
        } catch (NumberFormatException ignore) {
            return super.sub(property);
        }
    }
}
