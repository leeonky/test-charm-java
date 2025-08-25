package com.github.leeonky.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static com.github.leeonky.util.Sneaky.execute;

class FieldPropertyReader<T> extends FieldPropertyAccessor<T> implements PropertyReader<T> {

    FieldPropertyReader(BeanClass<T> beanClass, Field field) {
        super(beanClass, field);
    }

    @Override
    public Object getValue(T instance) {
        return execute(() -> getField().get(instance));
    }

    @Override
    public boolean isBeanProperty() {
        return !Modifier.isStatic(getField().getModifiers());
    }
}
