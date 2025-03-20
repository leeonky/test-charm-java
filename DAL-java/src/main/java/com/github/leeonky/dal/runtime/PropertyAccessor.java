package com.github.leeonky.dal.runtime;

import java.util.Objects;
import java.util.Set;

public interface PropertyAccessor<T> {

    default Object getValueByData(Data.Resolved data, Object property) {
        return getValue(data.value(), property);
    }

    Object getValue(T instance, Object property);

    Set<?> getPropertyNames(T instance);

    default boolean isNull(T instance) {
        return Objects.equals(instance, null);
    }
}
