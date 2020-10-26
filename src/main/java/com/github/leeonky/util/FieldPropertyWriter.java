package com.github.leeonky.util;

import java.lang.reflect.Field;

import static com.github.leeonky.util.Suppressor.run;

class FieldPropertyWriter<T> extends FieldPropertyAccessor<T> implements PropertyWriter<T> {

    FieldPropertyWriter(BeanClass<T> beanClass, Field field) {
        super(beanClass, field);
    }

    @Override
    public void setValue(T bean, Object value) {
        run(() -> field.set(bean, tryConvert(value)));
    }
}
