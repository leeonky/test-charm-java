package com.github.leeonky.util;

import java.lang.reflect.Field;

class FieldPropertyWriter<T> extends FieldProperty<T> implements PropertyWriter<T> {

    FieldPropertyWriter(BeanClass<T> beanClass, Field field) {
        super(beanClass, field);
    }

    @Override
    public void setValue(T bean, Object value) {
        try {
            field.set(bean, tryConvert(value));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

}
