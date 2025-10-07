package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.Optional;
import java.util.function.BiFunction;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;

abstract class Producer<T> {
    private final BeanClass<T> type;
    private final ValueCache<T> valueCache = new ValueCache<>();

    protected Producer(BeanClass<T> type) {
        this.type = type;
    }

    public BeanClass<T> getType() {
        return type;
    }

    protected abstract T produce();

    public T getValue() {
        return valueCache.cache(this::produce);
    }

    protected void collectConsistent(ObjectProducer<?> root, PropertyChain base) {
    }

    public Optional<Producer<?>> getChild(String property) {
        return Optional.empty();
    }

    protected void setChild(String property, Producer<?> producer) {
    }

    public Producer<?> childForUpdate(String property) {
        return getChild(property).orElse(null);
    }

    public Producer<?> childForRead(String property) {
        return getChild(property).orElseGet(() -> new ReadOnlyProducer<>(this, property));
    }

    @SuppressWarnings("unchecked")
    public <T> void changeChild(String property, Producer<T> producer) {
        Producer<T> origin = (Producer<T>) childForUpdate(property);
        if (origin != producer)
            setChild(property, origin == null ? producer : origin.changeTo(producer));
    }

    public Producer<?> descendantForRead(PropertyChain property) {
        return property.access(this, Producer::childForRead, identity());
    }

    public Producer<?> descendantForUpdate(PropertyChain property) {
        return property.access(this, (producer, subProperty) -> ofNullable(producer.childForUpdate(subProperty))
                .orElseGet(() -> new ReadOnlyProducer<>(producer, subProperty)), identity());
    }

    public void changeDescendant(PropertyChain property, BiFunction<Producer<?>, String, Producer<?>> producerFactory) {
        String tail = property.tail();
        property.removeTail().access(this, Producer::childForUpdate, Optional::ofNullable).ifPresent(lastObjectProducer ->
                lastObjectProducer.changeChild(tail, producerFactory.apply(lastObjectProducer, tail)));
    }

    public BeanClass<?> getPropertyWriterType(String property) {
        return getType().getPropertyWriter(property).getType();
    }

    public Optional<Producer<?>> newDefaultValueProducer(PropertyWriter<T> property) {
        return Optional.empty();
    }

    public Producer<T> changeTo(Producer<T> newProducer) {
        return newProducer.reChangeFrom(this);
    }

    protected Producer<T> reChangeFrom(Producer<T> producer) {
        return this;
    }

    protected Producer<T> changeFrom(ObjectProducer<T> producer) {
        return this;
    }

    protected Producer<T> changeFrom(OptionalSpecDefaultValueProducer<T> producer) {
        return this;
    }

    protected Producer<T> reChangeTo(DefaultValueProducer<T> newProducer) {
        return this;
    }

    protected Producer<T> reChangeTo(ConsistencyProducer<T, ?> newProducer) {
        return newProducer;
    }

    protected <R> void setupAssociation(String association, RootInstance<R> instance, ListPersistable cachedChildren) {
    }

    protected boolean isFixed() {
        return false;
    }

    public void verifyPropertyStructureDependent(T value) {
    }
}
