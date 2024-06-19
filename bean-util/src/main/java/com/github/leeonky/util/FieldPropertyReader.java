package com.github.leeonky.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static com.github.leeonky.util.Suppressor.get;

class FieldPropertyReader<T> extends FieldPropertyAccessor<T> implements PropertyReader<T> {

    FieldPropertyReader(BeanClass<T> beanClass, Field field) {
        super(beanClass, field);
    }

    @Override
    public Object getValue(T instance) {
        return get(() -> field.get(instance));
    }

    @Override
    public boolean isBeanProperty() {
        return !Modifier.isStatic(field.getModifiers());
    }
}
