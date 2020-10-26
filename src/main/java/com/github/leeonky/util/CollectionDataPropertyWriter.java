package com.github.leeonky.util;

import java.lang.reflect.Array;
import java.util.List;

class CollectionDataPropertyWriter<T> extends DataPropertyAccessor<T> implements PropertyWriter<T> {
    public CollectionDataPropertyWriter(BeanClass<T> beanClass, String name, BeanClass<?> type) {
        super(beanClass, name, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(T bean, Object value) {
        Class<T> type = getBeanType().getType();
        int index = Integer.valueOf(getName());
        if (type.isArray())
            Array.set(bean, index, value);
        else if (List.class.isAssignableFrom(type))
            ((List<Object>) bean).set(index, value);
        else
            throw new IllegalArgumentException(String.format("Cannot set element by index for %s", type.getName()));
    }
}
