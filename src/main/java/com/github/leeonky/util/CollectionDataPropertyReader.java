package com.github.leeonky.util;

import static java.lang.Integer.parseInt;

class CollectionDataPropertyReader<T> extends DataPropertyAccessor<T> implements PropertyReader<T> {
    CollectionDataPropertyReader(BeanClass<T> beanClass, String name, BeanClass<?> type) {
        super(beanClass, name, type);
    }

    @Override
    public Object getValue(T instance) {
        return CollectionHelper.toStream(instance).toArray()[parseInt(getName())];
    }
}
