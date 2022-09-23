package com.github.leeonky.util;

import java.util.function.BiConsumer;

public interface PropertyWriter<T> extends PropertyAccessor<T> {

    BiConsumer<T, Object> setter();

    default void setValue(T bean, Object value) {
        try {
            setter().accept(bean, value);
        } catch (CannotSetElementByIndexException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            String propertyName = getBeanType().isCollection() ? "[" + getName() + "]" : "." + getName();
            throw new IllegalArgumentException(String.format("Can not set %s to property %s%s<%s>",
                    value == null ? "null" : Classes.getClassName(value) + "[" + value + "]",
                    getBeanType().getName(), propertyName, getType().getName()), e);
        }
    }
}
