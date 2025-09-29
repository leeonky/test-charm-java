package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;

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
        ListConsistencyItem<T> listConsistencyItem = new ListConsistencyItem<>(property);
        items.add(listConsistencyItem);
        return new AbstractConsistency.LC1<>(consistency, this, listConsistencyItem);
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
                    consistency.property(combine(index, listConsistencyItem.property))
                            .read(s -> listConsistencyItem.composer.apply(new Object[]{s}))
                            .write(t -> listConsistencyItem.decomposer.apply(t)[0]);
                }
            }
        } else
            throw new IllegalStateException();
    }
}
