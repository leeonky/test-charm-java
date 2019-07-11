package com.github.leeonky.util;

import java.lang.reflect.Field;

class FieldPropertyReader<T> extends FieldProperty<T> implements PropertyReader<T> {

    FieldPropertyReader(BeanClass<T> beanClass, Field field) {
        super(beanClass, field);
    }

    @Override
    public Object getValue(T bean) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
