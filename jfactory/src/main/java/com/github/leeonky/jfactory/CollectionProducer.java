package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.Integer.max;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

class CollectionProducer<T, C> extends Producer<C> {
    private final List<Producer<?>> children = new ArrayList<>();
    private final BeanClass<T> parentType;
    private final CollectionInstance<T> collection;
    private final FactorySet factorySet;
    private Function<Integer, Producer<?>> elementDefaultValueProducerFactory;

    public CollectionProducer(BeanClass<T> parentType, BeanClass<C> collectionType,
                              SubInstance<T> instance, FactorySet factorySet) {
        super(collectionType);
        this.parentType = parentType;
        collection = instance.inCollection();
        this.factorySet = factorySet;
        elementDefaultValueProducerFactory = index -> new DefaultValueFactoryProducer<>(parentType,
                factorySet.getDefaultValueFactory(collectionType.getElementType()), collection.element(index));
    }

    public void changeElementDefaultValueProducerFactory(Function<Integer, Producer<?>> factory) {
        elementDefaultValueProducerFactory = factory;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected C produce() {
        return (C) getType().createCollection(children.stream().map(Producer::produce).collect(toList()));
    }

    @Override
    public Optional<Producer<?>> child(String property) {
        int index = parseInt(property);
        index = transformNegativeIndex(index);
        return Optional.ofNullable(index < children.size() ? children.get(index) : null);
    }

    @Override
    public void setChild(String property, Producer<?> producer) {
        int index = parseInt(property);
        fillCollectionWithDefaultValue(index);
        children.set(transformNegativeIndex(index), producer);
    }

    private int transformNegativeIndex(int index) {
        if (index < 0)
            index = children.size() + index;
        return index;
    }

    public int fillCollectionWithDefaultValue(int index) {
        int changed = 0;
        if (index >= 0) {
            for (int i = children.size(); i <= index; i++, changed++)
                children.add(defaultElementProducer(i));
        } else {
            int count = max(children.size(), -index) - children.size();
            for (int i = 0; i < count; i++, changed++)
                children.add(i, defaultElementProducer(i));
        }
        return changed;
    }

    public Producer<?> defaultElementProducer(int i) {
        return elementDefaultValueProducerFactory.apply(i);
    }

    @Override
    public Producer<?> childOrDefault(String property) {
        int index = parseInt(property);
        fillCollectionWithDefaultValue(index);
        return children.get(transformNegativeIndex(index));
    }

    @Override
    protected void doDependencies() {
        children.forEach(Producer::doDependencies);
    }

    @Override
    protected void collectLinks(ObjectProducer<?> root, PropertyChain base) {
        range(0, children.size()).forEach(i ->
                children.get(i).collectLinks(root, base.concat(String.valueOf(i))));
    }

    @Override
    public Optional<Producer<?>> createPropertyDefaultValueProducer(PropertyWriter<?> property) {
        return factorySet.queryDefaultValueFactory(getType().getElementType()).map(builder ->
                new DefaultValueFactoryProducer<>(parentType, builder, collection.element(parseInt(property.getName()))));
    }

    @Override
    protected <T> void setupAssociation(String association, RootInstance<T> instance, ListPersistable cachedChildren) {
        children.stream().filter(ObjectProducer.class::isInstance).map(ObjectProducer.class::cast).forEach(objectProducer ->
                objectProducer.setupAssociation(association, instance, cachedChildren));

    }

    public int childrenCount() {
        return children.size();
    }
}
