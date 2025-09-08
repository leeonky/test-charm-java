package com.github.leeonky.util;

import java.util.List;

public class PropertyReaderDecorator<T> extends PropertyAccessorDecorator<T, PropertyReader<T>> implements PropertyReader<T> {

    public PropertyReaderDecorator(PropertyReader<T> reader) {
        super(reader);
    }

    @Override
    public Object getValue(T instance) {
        return accessor.getValue(instance);
    }

    @Override
    public PropertyReader<?> getPropertyChainReader(List<Object> chain) {
        return accessor.getPropertyChainReader(chain);
    }
}
