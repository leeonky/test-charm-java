package com.github.leeonky.jfactory;

import com.github.leeonky.util.PropertyWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class CollectionInstance<T> extends SubInstance<T> {
    private final List<Integer> indexes;

    public CollectionInstance(List<Integer> indexes, PropertyWriter<?> property,
                              Spec<T> spec, Arguments argument, TypeSequence.Sequence sequence) {
        super(property, spec, argument, sequence);
        this.indexes = new ArrayList<>(indexes);
    }

    @Override
    public String propertyInfo() {
        return String.format("%s%s", super.propertyInfo(),
                indexes.stream().map(i -> String.format("[%d]", i)).collect(Collectors.joining()));
    }

    @Override
    public SubInstance<T> sub(PropertyWriter<?> property) {
        try {
            CollectionInstance<T> collection = new CollectionInstance<>(indexes, getProperty(), spec, arguments, sequence);
            collection.indexes.add(Integer.parseInt(property.getName()));
            return collection;
        } catch (NumberFormatException ignore) {
            return super.sub(property);
        }
    }
}
