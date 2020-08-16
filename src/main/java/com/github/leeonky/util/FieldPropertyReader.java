package com.github.leeonky.util;

import java.lang.reflect.Field;

import static com.github.leeonky.util.Suppressor.get;

class FieldPropertyReader<T> extends FieldProperty<T> implements PropertyReader<T> {

    FieldPropertyReader(BeanClass<T> beanClass, Field field) {
        super(beanClass, field);
    }

    @Override
    public Object getValue(T bean) {
        return get(() -> field.get(bean));
    }
}
