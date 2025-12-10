package com.github.leeonky.jfactory;

import java.util.Collection;
import java.util.Collections;

public interface DataRepository extends Persistable {
    default <T> Collection<T> queryAll(Class<T> type) {
        return Collections.emptyList();
    }

    default void clear() {
    }
}
