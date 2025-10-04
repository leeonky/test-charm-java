package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.github.leeonky.jfactory.DefaultConsistency.LINK_COMPOSER;
import static com.github.leeonky.jfactory.DefaultConsistency.LINK_DECOMPOSER;
import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.IntStream.range;

class DefaultListConsistency<T> implements ListConsistency<T> {
    final PropertyChain listProperty;
    private final Consistency<T> consistency;
    final List<ListConsistencyItem<T>> items = new ArrayList<>();
    private final List<DefaultListConsistency<?>> list = new ArrayList<>();

    DefaultListConsistency(String listProperty, Consistency<T> consistency) {
        this.listProperty = propertyChain(listProperty);
        this.consistency = consistency;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ListConsistency<T> direct(String property) {
        return property(property).read((Function<Object, T>) LINK_COMPOSER).write((Function<T, Object>) LINK_DECOMPOSER);
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

    @Override
    public <P1, P2, P3> LC3<T, P1, P2, P3> properties(String property1, String property2, String property3) {
        ListConsistencyItem<T> listConsistencyItem = new ListConsistencyItem<>(asList(property1, property2, property3));
        items.add(listConsistencyItem);
        return new LC3<>(this, listConsistencyItem);
    }

    @Deprecated
    void populateConsistencies(ObjectProducer<?> producer, PropertyChain parentList) {
        PropertyChain listProperty = parentList.concat(this.listProperty);
        Producer<?> descendant = producer.descendantForUpdate(listProperty);
        if (!(descendant instanceof CollectionProducer))
            throw new IllegalStateException(listProperty + " is not List");
        range(0, ((CollectionProducer<?, ?>) descendant).childrenCount()).mapToObj(listProperty::concat)
                .forEach(elementProperty -> populateElementConsistency(producer, elementProperty));
    }

    private void populateElementConsistency(ObjectProducer<?> producer, PropertyChain elementProperty) {
        items.forEach(item -> item.populateConsistency(elementProperty, consistency));
        list.forEach(listConsistency -> listConsistency.populateConsistencies(producer, elementProperty));
    }

//    @Override
//    public NestedListConsistencyBuilder<T> list(String property) {
//        DefaultListConsistency<T> listConsistency = new DefaultListConsistency<>(property, consistency);
//        list.add(listConsistency);
//        return new NestedListConsistencyBuilder<>(this, listConsistency);
//    }
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

    @Override
    public <P1, P2, P3> LC3<T, P1, P2, P3> properties(String property1, String property2, String property3) {
        return delegate.properties(property1, property2, property3);
    }

//    @Override
//    public NestedListConsistencyBuilder<T> list(String property) {
//        return delegate.list(property);
//    }
}

class MultiPropertyListConsistency<T, C extends MultiPropertyListConsistency<T, C>> extends DecorateListConsistency<T> {
    final ListConsistencyItem<T> last;

    MultiPropertyListConsistency(ListConsistency<T> delegate, ListConsistencyItem<T> last) {
        super(delegate);
        this.last = last;
    }

    @SuppressWarnings("unchecked")
    public C read(Function<Object[], T> composer) {
        last.setComposer(new ComposerWrapper<>(composer, composer));
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C write(Function<T, Object[]> decomposer) {
        last.setDecomposer(new DecomposerWrapper<>(decomposer, decomposer));
        return (C) this;
    }
}
