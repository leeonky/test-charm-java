package com.github.leeonky.util;

import java.lang.reflect.Field;

class FieldPropertyWriter<T> implements PropertyWriter<T> {
    private final Field field;

    public FieldPropertyWriter(Field field) {
        this.field = field;
    }

    @Override
    public void setValue(T bean, Object value) {
        try {
            field.set(bean, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getName() {
        return field.getName();
    }
}
