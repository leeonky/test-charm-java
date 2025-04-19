package com.github.leeonky.dal.runtime;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface AdaptiveList<T> {
    DALCollection<T> list();

    List<T> soloList();

    default T only() {
        return soloList().get(0);
    }

    static <T> AdaptiveList<T> staticList(Collection<T> list) {
        return new StaticAdaptiveList<>(new CollectionDALCollection<>(list));
    }

    static <T> AdaptiveList<T> staticList(Supplier<T> supplier) {
        return new StaticAdaptiveList<>(new InfiniteDALCollection<>(supplier));
    }
}
