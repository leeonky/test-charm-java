package com.github.leeonky.util;

import java.util.LinkedList;
import java.util.List;

public interface PropertyReader<T> extends Property<T> {
    Object getValue(T bean);

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
}
