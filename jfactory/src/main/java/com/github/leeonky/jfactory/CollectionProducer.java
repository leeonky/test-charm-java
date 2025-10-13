package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.lang.Integer.max;
import static java.lang.Integer.parseInt;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

class CollectionProducer<T, C> extends Producer<C> {
    private final List<Producer<?>> children = new ArrayList<>();
    private final BeanClass<T> parentType;
    private final ObjectCollection<T> collection;
    private final FactorySet factorySet;
    private final JFactory jFactory;
    private Function<PropertyWriter<C>, Producer<?>> elementPopulationFactory = any -> null;

    public CollectionProducer(BeanClass<T> parentType, BeanClass<C> collectionType,
                              ObjectProperty<T> instance, FactorySet factorySet, JFactory jFactory) {
        super(collectionType);
        this.parentType = parentType;
        collection = instance.asCollection();
        this.factorySet = factorySet;
        this.jFactory = jFactory;
    }

    public void changeElementPopulationFactory(Function<PropertyWriter<C>, Producer<?>> factory) {
        elementPopulationFactory = factory;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected C produce() {
        return (C) getType().createCollection(children.stream().map(Producer::getValue).collect(toList()));
    }

    @Override
    public Optional<Producer<?>> getChild(String property) {
        int index;
        try {
            index = transformNegativeIndex(parseInt(property));
        } catch (NumberFormatException ignore) {
            return of(new ReadOnlyProducer<>(this, property));
        }
        return ofNullable(index < children.size() && index >= 0 ? children.get(index) : null);
    }

    @Override
    protected void setChild(String property, Producer<?> producer) {
        int index = parseInt(property);
        fillCollectionWithDefaultValue(index);
        children.set(transformNegativeIndex(index), producer);
    }

    private int transformNegativeIndex(int index) {
        return index < 0 ? children.size() + index : index;
    }

    public int fillCollectionWithDefaultValue(int index) {
        int changed = 0;
        if (index >= 0) {
            for (int i = children.size(); i <= index; i++, changed++)
                children.add(newElementPopulationProducer(getType().getPropertyWriter(String.valueOf(i))));
        } else {
            int count = max(children.size(), -index) - children.size();
            for (int i = 0; i < count; i++, changed++)
                children.add(i, newElementPopulationProducer(getType().getPropertyWriter(String.valueOf(i))));
        }
        return changed;
    }

    public Producer<?> newElementPopulationProducer(PropertyWriter<C> propertyWriter) {
        return getFirstPresent(() -> ofNullable(elementPopulationFactory.apply(propertyWriter)),
                () -> newDefaultValueProducer(propertyWriter))
                .orElseGet(() -> new DefaultTypeValueProducer<>(propertyWriter.getType()));
    }

    public Producer<?> newDefaultElementProducer(PropertyWriter<C> propertyWriter) {
        return getFirstPresent(() -> ofNullable(elementPopulationFactory.apply(propertyWriter)),
                () -> newDefaultValueProducer(propertyWriter))
                .orElseGet(() -> jFactory.type(propertyWriter.getType()).createProducer());
    }

    @Override
    public Producer<?> childForUpdate(String property) {
        return getChild(property).orElseGet(() -> {
            Producer<?> producer = newDefaultElementProducer(getType().getPropertyWriter(property));
            setChild(property, producer);
            return producer;
        });
    }

    @Override
    public Producer<?> childForRead(String property) {
        return getChild(property).orElse(PlaceHolderProducer.PLACE_HOLDER);
    }

    @Override
    protected void collectConsistent(ObjectProducer<?> root, PropertyChain base) {
        range(0, children.size()).forEach(i ->
                children.get(i).collectConsistent(root, base.concat(String.valueOf(i))));
    }

    @Override
    public Optional<Producer<?>> newDefaultValueProducer(PropertyWriter<C> property) {
        return factorySet.newDefaultValueFactoryProducer(parentType, property, collection.sub(property));
    }

    @Override
    protected <R> void setupAssociation(String association, ObjectInstance<R> instance, ListPersistable cachedChildren) {
        children.stream().filter(ObjectProducer.class::isInstance).map(ObjectProducer.class::cast).forEach(objectProducer ->
                objectProducer.setupAssociation(association, instance, cachedChildren));
    }

    public int childrenCount() {
        return children.size();
    }

    @Override
    protected boolean isFixed() {
        return children.stream().anyMatch(Producer::isFixed);
    }

    @Override
    public void verifyPropertyStructureDependent() {
        children.forEach(Producer::verifyPropertyStructureDependent);
    }
}
