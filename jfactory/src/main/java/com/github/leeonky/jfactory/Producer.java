package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.Optional;
import java.util.function.BiFunction;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;

abstract class Producer<T> {
    public static final Producer PLACE_HOLDER = new Producer<Object>(BeanClass.create(Object.class)) {
        @Override
        protected Object produce() {
            throw new IllegalStateException("This is a place holder producer, can not produce any value");
        }
    };
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

    public void setChild(String property, Producer<?> producer) {
    }

    public Optional<Producer<?>> child(String property) {
        return Optional.empty();
    }

    public Producer<?> childOrDefault(String property) {
        return child(property).orElse(null);
    }

    public Producer<?> descendant(PropertyChain property) {
        return property.access(this, (producer, subProperty) -> ofNullable(producer.childOrDefault(subProperty))
                .orElseGet(() -> new ReadOnlyProducer<>(producer, subProperty)), identity());
    }

    public void changeDescendant(PropertyChain property, BiFunction<Producer<?>, String, Producer<?>> producerFactory) {
        String tail = property.tail();
        property.removeTail().access(this, Producer::childOrDefault, Optional::ofNullable).ifPresent(lastObjectProducer ->
                lastObjectProducer.changeChild(tail, producerFactory.apply(lastObjectProducer, tail)));
    }

    @SuppressWarnings("unchecked")
    public <T> void changeChild(String property, Producer<T> producer) {
        Producer<T> origin = (Producer<T>) childOrDefault(property);
        if (origin != producer)
            setChild(property, origin == null ? producer : origin.changeTo(producer));
    }

    public BeanClass<?> getPropertyWriterType(String property) {
        return getType().getPropertyWriter(property).getType();
    }

    public Optional<Producer<?>> createPropertyDefaultValueProducer(PropertyWriter<?> property) {
        return Optional.empty();
    }

    public Producer<T> getLinkOrigin() {
        return this;
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

    protected <T> void setupAssociation(String association, RootInstance<T> instance, ListPersistable cachedChildren) {
    }

    protected boolean isFixed() {
        return false;
    }
}
