package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.leeonky.jfactory.DefaultConsistency.LINK_COMPOSER;
import static com.github.leeonky.jfactory.DefaultConsistency.LINK_DECOMPOSER;
import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static com.github.leeonky.util.Zipped.zip;
import static com.github.leeonky.util.function.Extension.notAllowParallelReduce;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;

class DefaultListConsistency<T, C extends Coordinate> implements ListConsistency<T, C> {
    final List<PropertyChain> listProperty;
    private final DefaultConsistency<T, C> consistency;
    final List<ListConsistencyItem<T>> items = new ArrayList<>();
    @Deprecated
    private final List<DefaultListConsistency<?, ?>> list = new ArrayList<>();
    private Function<Coordinate, C> aligner = this::convert;

    private C convert(Coordinate e) {
        return e.convertTo(consistency.coordinateType());
    }

    private Function<C, Coordinate> inverseAligner = e -> e;

    DefaultListConsistency(List<String> listProperty, DefaultConsistency<T, C> consistency) {
        this.listProperty = listProperty.stream().map(PropertyChain::propertyChain).collect(Collectors.toList());
        this.consistency = consistency;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ListConsistency<T, C> direct(String property) {
        return property(property).read((Function<Object, T>) LINK_COMPOSER).write((Function<T, Object>) LINK_DECOMPOSER);
    }

    @Override
    public <P> ListConsistency.LC1<T, P, C> property(String property) {
        ListConsistencyItem<T> listConsistencyItem = new ListConsistencyItem<>(singletonList(property));
        items.add(listConsistencyItem);
        return new DefaultListConsistency.LC1<>(this, listConsistencyItem);
    }

    @Override
    public <P1, P2> ListConsistency.LC2<T, P1, P2, C> properties(String property1, String property2) {
        ListConsistencyItem<T> listConsistencyItem = new ListConsistencyItem<>(asList(property1, property2));
        items.add(listConsistencyItem);
        return new DefaultListConsistency.LC2<>(this, listConsistencyItem);
    }

    @Override
    public <P1, P2, P3> LC3<T, P1, P2, P3, C> properties(String property1, String property2, String property3) {
        ListConsistencyItem<T> listConsistencyItem = new ListConsistencyItem<>(asList(property1, property2, property3));
        items.add(listConsistencyItem);
        return new LC3<>(this, listConsistencyItem);
    }

    @Deprecated
    void populateConsistencies(ObjectProducer<?> producer, PropertyChain parentList) {
        PropertyChain listProperty = parentList.concat(this.listProperty.get(0));
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

    Optional<PropertyChain> toProperty(C coordinate) {
        return ofNullable(inverseAligner.apply(coordinate)).map(co -> zip(listProperty, co.indexes()).stream().reduce(propertyChain(""),
                (p, z) -> p.concat(z.left()).concat(z.right().index()), notAllowParallelReduce()));
    }

    List<DefaultConsistency<T, C>> collectCoordinateAndProcess(ObjectProducer<?> producer, List<Index> baseIndex,
                                                               int l, PropertyChain baseProperty) {
        List<DefaultConsistency<T, C>> results = new ArrayList<>();
        PropertyChain list = baseProperty.concat(listProperty.get(l++));
        CollectionProducer<?, ?> collectionProducer = (CollectionProducer<?, ?>) producer.descendantForUpdate(list);
        for (int i = 0; i < collectionProducer.childrenCount(); i++) {
            Index index = new Index(collectionProducer.childrenCount(), i);
            List<Index> indexes = new ArrayList<>(baseIndex);
            indexes.add(index);
            if (listProperty.size() > l)
                results.addAll(collectCoordinateAndProcess(producer, indexes, l, list.concat(i)));
            else
                ofNullable(aligner.apply(new Coordinate(indexes))).ifPresent(c ->
                        results.add(consistency.populateConsistencyWithList(c)));
        }
        return results;
    }

    public void normalize(Function<Coordinate, C> aligner,
                          Function<C, Coordinate> inverseAligner) {
        this.aligner = aligner;
        this.inverseAligner = inverseAligner;
    }

    //    @Override
//    public NestedListConsistencyBuilder<T> list(String property) {
//        DefaultListConsistency<T> listConsistency = new DefaultListConsistency<>(property, consistency);
//        list.add(listConsistency);
//        return new NestedListConsistencyBuilder<>(this, listConsistency);
//    }
}

class DecorateListConsistency<T, C extends Coordinate> implements ListConsistency<T, C> {
    private final ListConsistency<T, C> delegate;

    public DecorateListConsistency(ListConsistency<T, C> delegate) {
        this.delegate = delegate;
    }

    @Override
    public ListConsistency<T, C> direct(String property) {
        return delegate.direct(property);
    }

    @Override
    public <P> ListConsistency.LC1<T, P, C> property(String property) {
        return delegate.property(property);
    }

    @Override
    public <P1, P2> ListConsistency.LC2<T, P1, P2, C> properties(String property1, String property2) {
        return delegate.properties(property1, property2);
    }

    @Override
    public <P1, P2, P3> LC3<T, P1, P2, P3, C> properties(String property1, String property2, String property3) {
        return delegate.properties(property1, property2, property3);
    }

    //    @Override
//    public NestedListConsistencyBuilder<T> list(String property) {
//        return delegate.list(property);
//    }
}

class MultiPropertyListConsistency<T, M extends MultiPropertyListConsistency<T, M, C>, C extends Coordinate>
        extends DecorateListConsistency<T, C> {
    final ListConsistencyItem<T> last;

    MultiPropertyListConsistency(ListConsistency<T, C> delegate, ListConsistencyItem<T> last) {
        super(delegate);
        this.last = last;
    }

    @SuppressWarnings("unchecked")
    public M read(Function<Object[], T> composer) {
        last.setComposer(new ComposerWrapper<>(composer, composer));
        return (M) this;
    }

    @SuppressWarnings("unchecked")
    public M write(Function<T, Object[]> decomposer) {
        last.setDecomposer(new DecomposerWrapper<>(decomposer, decomposer));
        return (M) this;
    }
}
