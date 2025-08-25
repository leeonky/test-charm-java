package com.github.leeonky.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;

import static com.github.leeonky.util.Sneaky.executeVoid;

class FieldPropertyWriter<T> extends FieldPropertyAccessor<T> implements PropertyWriter<T> {
    private final BiConsumer<T, Object> SETTER = (bean, value) -> executeVoid(() -> getField().set(bean, tryConvert(value)));

    FieldPropertyWriter(BeanClass<T> beanClass, Field field) {
        super(beanClass, field);
    }

    @Override
    public BiConsumer<T, Object> setter() {
        return SETTER;
    }

    @Override
    public void setValue(T bean, Object value) {
        try {
            setter().accept(bean, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Can not set %s to property %s.%s<%s>",
                    value == null ? "null" : Classes.getClassName(value) + "[" + value + "]",
                    getBeanType().getName(), getName(), getType().getName()), e);
        }
    }

    @Override
    public boolean isBeanProperty() {
        return !Modifier.isStatic(getField().getModifiers());
    }
}
