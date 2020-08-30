package com.github.leeonky.util;

class CollectionDataPropertyReader<T> extends DataProperty<T> implements PropertyReader<T> {
    CollectionDataPropertyReader(BeanClass<T> beanClass, String name, BeanClass<?> type) {
        super(beanClass, name, type);
    }

    @Override
    public Object getValue(T bean) {
        return BeanClass.arrayCollectionToStream(bean).toArray()[Integer.valueOf(getName())];
    }
}
