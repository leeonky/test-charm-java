package org.testcharm.util;

import java.util.function.BiConsumer;

import static org.testcharm.util.AbstractPropertyAccessor.propertyNameInChain;

public class PropertyWriterDecorator<T> extends PropertyAccessorDecorator<T, PropertyWriter<T>> implements PropertyWriter<T> {
    public PropertyWriterDecorator(PropertyWriter<T> writer) {
        super(writer);
    }

    @Override
    public BiConsumer<T, Object> setter() {
        return accessor.setter();
    }

    @Override
    public void setValue(T bean, Object value) {
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
}
