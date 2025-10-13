package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

class DefaultValueFactoryProducer<T, V> extends DefaultValueProducer<V> {

    public DefaultValueFactoryProducer(BeanClass<T> beanType, DefaultValueFactory<V> factory, ObjectProperty<T> instance) {
        super(BeanClass.create(factory.getType()), () -> factory.create(beanType, instance));
    }
}
