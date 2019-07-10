package com.github.leeonky.util;

import java.lang.reflect.Field;

class FieldPropertyReader<T> extends FieldProperty implements PropertyReader<T> {

    FieldPropertyReader(Field field) {
        super(field);
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
