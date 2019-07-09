package com.github.leeonky.util;

import java.lang.reflect.Field;

class FieldPropertyReader<T> implements PropertyReader<T> {
    private final Field field;

    FieldPropertyReader(Field field) {
        this.field = field;
    }

    @Override
    public Object getValue(T bean) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Class<?> getPropertyType() {
        return field.getType();
    }
}
