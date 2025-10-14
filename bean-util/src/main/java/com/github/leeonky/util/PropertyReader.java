package com.github.leeonky.util;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public interface PropertyReader<T> extends PropertyAccessor<T> {
    Object getValue(T instance);

    default PropertyReader<?> getPropertyChainReader(List<Object> chain) {
        PropertyReader<?> reader = this;
        LinkedList<Object> linkedChain = new LinkedList<>(chain);
        while (!linkedChain.isEmpty()) {
            Object p = linkedChain.removeFirst();
            if (p instanceof Integer)
                return BeanClass.create(getType().getElementType().getType()).getPropertyChainReader(linkedChain);
            else
                reader = getType().getPropertyReader((String) p);
        }
        return reader;
    }

    default PropertyReader<T> decorateType(BeanClass<?> newType) {
        if (newType == getType())
            return this;
        return new PropertyReaderDecorator<T>(this) {
            @Override
            public Type getGenericType() {
                return newType.getGenericType();
            }
        };
    }
}
