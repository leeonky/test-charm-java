package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.runtime.PropertyAccessor;

import java.util.Map;
import java.util.Set;

public class MapPropertyAccessor implements PropertyAccessor<Map<?, ?>> {
    @Override
    public Object getValue(Map<?, ?> instance, Object property) {
        return instance.get(property);
    }

    @Override
    public Set<?> getPropertyNames(Map<?, ?> instance) {
        return instance.keySet();
    }
}
