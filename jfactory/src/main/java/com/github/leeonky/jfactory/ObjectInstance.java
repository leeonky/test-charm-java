package com.github.leeonky.jfactory;

import com.github.leeonky.util.PropertyWriter;

import java.util.function.Consumer;
import java.util.function.Supplier;

class ObjectInstance<T> implements Instance<T> {
    private static final Object[] NO_TRAIT_PARAMS = new Object[0];
    protected final Spec<T> spec;
    protected final Arguments arguments;
    protected final TypeSequence.Sequence sequence;
    private final ValueCache<T> valueCache = new ValueCache<>();
    private int collectionSize = 0;
    private Object[] traitParams = NO_TRAIT_PARAMS;

    public ObjectInstance(Spec<T> spec, Arguments arguments, TypeSequence.Sequence sequence) {
        this.spec = spec;
        this.arguments = arguments;
        this.sequence = sequence;
    }

    @Override
    public int getSequence() {
        return sequence.get();
    }

    ObjectProperty<T> sub(PropertyWriter<?> property) {
        return new ObjectProperty<>(property, this);
    }

    @Override
    public Spec<T> spec() {
        return spec;
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

    @Override
    public Object[] traitParams() {
        return traitParams;
    }

    void runTraitWithParams(Object[] params, Consumer<Spec<T>> action) {
        traitParams = params;
        try {
            action.accept(spec);
        } finally {
            traitParams = NO_TRAIT_PARAMS;
        }
    }
}
