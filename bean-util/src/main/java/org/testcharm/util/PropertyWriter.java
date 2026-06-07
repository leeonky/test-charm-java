package org.testcharm.util;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;

import static org.testcharm.util.AbstractPropertyAccessor.propertyNameInChain;

public interface PropertyWriter<T> extends PropertyAccessor<T> {

    BiConsumer<T, Object> setter();

    default void setValue(T bean, Object value) {
        try {
            setter().accept(bean, tryConvert(value));
        } catch (CannotSetElementByIndexException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Can not set %s to property %s%s<%s>",
                    value == null ? "null" : Classes.getClassName(value) + "[" + value + "]",
                    getBeanType().getName(), propertyNameInChain(this), getType().getName()), e);
        }
    }

    default PropertyWriter<T> decorateType(BeanClass<?> newType) {
        if (newType == getType())
            return this;
        return new PropertyWriterDecorator<T>(this) {
            @Override
            public Type getGenericType() {
                return newType.getGenericType();
            }
        };
    }
}
