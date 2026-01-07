package com.github.leeonky.jfactory;

import java.util.function.Consumer;

class SpecInstanceFactory<T, S extends Spec<T>> extends SpecClassFactory<T> {
    private final S spec;
    private final Consumer<S> trait;

    @SuppressWarnings("unchecked")
    public SpecInstanceFactory(FactorySet factorySet, S spec, Consumer<S> trait) {
        super((Class<? extends Spec<T>>) spec.getClass(), factorySet, false);
        this.spec = spec;
        this.trait = trait;
    }

    @Override
    protected Spec<T> newSpecInstance() {
        return spec;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void collectSubSpec(Spec<T> spec_) {
        super.collectSubSpec(spec_);
        collectClassSpec(spec -> trait.accept((S) spec), spec_);
    }
}
