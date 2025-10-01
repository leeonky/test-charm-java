package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Optional;

class DefaultValueFactoryProducer<T, V> extends DefaultValueProducer<V> {

    public DefaultValueFactoryProducer(BeanClass<T> beanType, DefaultValueFactory<V> factory, SubInstance<T> instance) {
        super(BeanClass.create(factory.getType()), () -> factory.create(beanType, instance));
    }

//    @Override
//    public Optional<Producer<?>> getChild(String property) {
//        return Optional.of(new ReadOnlyProducer<>(this, property));
//    }

    @Override
    public Optional<Producer<?>> getChild(String property) {
        return Optional.empty();
    }

    @Override
    public Producer<?> childForRead(String property) {
        return getChild(property).orElseGet(() -> new ReadOnlyProducer<>(this, property));
    }
}
