package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

class DefaultValueFactoryProducer<T, V> extends DefaultValueProducer<V> {

    public DefaultValueFactoryProducer(BeanClass<T> beanType, DefaultValueFactory<V> factory, ObjectProperty<T> instance) {
        super(JFactory.beanClass(factory.getType()), () -> factory.create(beanType, instance));
    }
}
