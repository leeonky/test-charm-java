package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

class DefaultListConsistency<T> implements ListConsistency<T> {
    private final PropertyChain listProperty;
    private final DefaultConsistency<T> consistency;
    private final List<ListConsistencyItem<T>> items = new ArrayList<>();

    public DefaultListConsistency(String listProperty, DefaultConsistency<T> consistency) {
        this.listProperty = propertyChain(listProperty);
        this.consistency = consistency;
    }

    @Override
    public ListConsistency<T> direct(String property) {
        this.<T>property(property).read(s -> s).write(s -> s);
        return this;
    }

    @Override
    public <P> AbstractConsistency.LC1<T, P> property(String property) {
        ListConsistencyItem<T> listConsistencyItem = new ListConsistencyItem<>(singletonList(property));
        items.add(listConsistencyItem);
        return new AbstractConsistency.LC1<>(consistency, this, listConsistencyItem);
    }

    @Override
    public <P1, P2> AbstractConsistency.LC2<T, P1, P2> properties(String property1, String property2) {
        ListConsistencyItem<T> listConsistencyItem = new ListConsistencyItem<>(asList(property1, property2));
        items.add(listConsistencyItem);
        return new AbstractConsistency.LC2<>(consistency, this, listConsistencyItem);
    }

    private String combine(int index, String property) {
        return listProperty.toString() + String.format("[%d].", index) + property;
    }

    public void resolveToItems(ObjectProducer<?> producer) {
        Producer<?> descendant = producer.descendant(listProperty);
        if (descendant instanceof CollectionProducer) {
            CollectionProducer<?, ?> collectionProducer = (CollectionProducer) descendant;
            int count = collectionProducer.childrenCount();
            for (int i = 0; i < count; i++) {
                int index = i;
                for (ListConsistencyItem<T> listConsistencyItem : items) {
                    consistency.properties(listConsistencyItem.property.stream().map(p -> combine(index, p)).toArray(String[]::new))
                            .read(listConsistencyItem.composer)
                            .write(listConsistencyItem.decomposer);
                }
            }
        } else
            throw new IllegalStateException();
    }
}
