package org.testcharm.jfactory;

import java.util.Optional;

import static java.util.Optional.of;
import static org.testcharm.jfactory.JFactory.beanClass;

class PlaceHolderProducer extends Producer<Object> {
    static final Producer<?> PLACE_HOLDER = new PlaceHolderProducer();

    public PlaceHolderProducer() {
        super(beanClass(Object.class));
    }

    @Override
    protected Object produce() {
        throw new IllegalStateException("This is a place holder producer, can not produce any value");
    }

    @Override
    public Optional<Producer<?>> getChild(String property) {
        return of(PLACE_HOLDER);
    }
}
