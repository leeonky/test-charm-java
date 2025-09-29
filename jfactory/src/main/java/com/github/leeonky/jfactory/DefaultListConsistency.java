package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

class DefaultListConsistency<T> implements ListConsistency<T> {
    private final PropertyChain listProperty;
    private final Consistency<T> consistency;
    private final List<ListConsistencyItem<T>> items = new ArrayList<>();

    DefaultListConsistency(String listProperty, Consistency<T> consistency) {
        this.listProperty = propertyChain(listProperty);
        this.consistency = consistency;
    }

    @Override
    public ListConsistency<T> direct(String property) {
        this.<T>property(property).read(s -> s).write(s -> s);
        return this;
    }

    @Override
    public <P> ListConsistency.LC1<T, P> property(String property) {
        ListConsistencyItem<T> listConsistencyItem = new ListConsistencyItem<>(singletonList(property));
        items.add(listConsistencyItem);
        return new DefaultListConsistency.LC1<>(this, listConsistencyItem);
    }

    @Override
    public <P1, P2> ListConsistency.LC2<T, P1, P2> properties(String property1, String property2) {
        ListConsistencyItem<T> listConsistencyItem = new ListConsistencyItem<>(asList(property1, property2));
        items.add(listConsistencyItem);
        return new DefaultListConsistency.LC2<>(this, listConsistencyItem);
    }

    private String combine(int index, String property) {
        return listProperty.toString() + String.format("[%d].", index) + property;
    }

    void resolveToItems(ObjectProducer<?> producer) {
        Producer<?> descendant = producer.descendant(listProperty);
        if (descendant instanceof CollectionProducer) {
            CollectionProducer<?, ?> collectionProducer = (CollectionProducer<?, ?>) descendant;
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

class DecorateListConsistency<T> implements ListConsistency<T> {
    private final ListConsistency<T> delegate;

    public DecorateListConsistency(ListConsistency<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public ListConsistency<T> direct(String property) {
        return delegate.direct(property);
    }

    @Override
    public <P> ListConsistency.LC1<T, P> property(String property) {
        return delegate.property(property);
    }

    @Override
    public <P1, P2> ListConsistency.LC2<T, P1, P2> properties(String property1, String property2) {
        return delegate.properties(property1, property2);
    }
}
