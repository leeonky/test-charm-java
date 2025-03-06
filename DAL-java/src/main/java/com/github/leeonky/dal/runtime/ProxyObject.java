package com.github.leeonky.dal.runtime;

import java.util.Collections;
import java.util.Set;

public interface ProxyObject {
    Object getValue(Object property);

    default Set<?> getPropertyNames() {
        return Collections.emptySet();
    }

    default boolean isNull() {
        return false;
    }
}
