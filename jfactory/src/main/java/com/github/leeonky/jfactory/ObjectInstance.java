package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.function.Consumer;
import java.util.function.Supplier;

class ObjectInstance<T> implements Instance<T> {
    private final Arguments arguments;
    private final TypeSequence.Sequence sequence;
    private final BeanClass<T> type;
    private final ValueCache<T> valueCache = new ValueCache<>();
    private int collectionSize = 0;

    public ObjectInstance(Arguments arguments, TypeSequence.Sequence sequence, BeanClass<T> type) {
        this.arguments = arguments;
        this.sequence = sequence;
        this.type = type;
    }

    @Override
    public BeanClass<T> type() {
        return type;
    }

    @Override
    public int getSequence() {
        return sequence.get();
    }

    ObjectProperty<T> sub(PropertyWriter<?> property) {
        return new ObjectProperty<>(property, this);
    }

    @Override
    public Supplier<T> reference() {
        return valueCache::getValue;
    }

    @Override
    public <P> P param(String key) {
        return arguments.param(key);
    }

    @Override
    public <P> P param(String key, P defaultValue) {
        return arguments.param(key, defaultValue);
    }

    @Override
    public Arguments params(String property) {
        return arguments.params(property);
    }

    @Override
    public Arguments params() {
        return arguments;
    }

    T cache(Supplier<T> supplier, Consumer<T> operation) {
        return valueCache.cache(supplier, operation);
    }

    public void setCollectionSize(int collectionSize) {
        this.collectionSize = collectionSize;
    }

    @Override
    public int collectionSize() {
        return collectionSize;
    }
}
