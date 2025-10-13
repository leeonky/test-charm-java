package com.github.leeonky.jfactory;

import com.github.leeonky.util.PropertyWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ObjectCollectionElement<T> extends ObjectProperty<T> {
    private final List<Integer> indexes;

    ObjectCollectionElement(List<Integer> indexes, PropertyWriter<?> property, Instance<T> instance) {
        super(property, instance);
        this.indexes = new ArrayList<>(indexes);
    }

    @Override
    public String propertyInfo() {
        return String.format("%s%s", super.propertyInfo(),
                indexes.stream().map(i -> String.format("[%d]", i)).collect(Collectors.joining()));
    }
}
