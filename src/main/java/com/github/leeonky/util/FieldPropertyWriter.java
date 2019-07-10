package com.github.leeonky.util;

import java.lang.reflect.Field;

class FieldPropertyWriter<T> extends FieldProperty implements PropertyWriter<T> {
    FieldPropertyWriter(Field field) {
        super(field);
    }

    @Override
    public void setValue(T bean, Object value) {
        try {
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
