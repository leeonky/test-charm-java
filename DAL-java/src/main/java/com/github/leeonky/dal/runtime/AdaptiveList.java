package com.github.leeonky.dal.runtime;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface AdaptiveList<T> {
    DALCollection<T> list();

    default boolean isEmpty() {
        return list().isEmpty();
    }

    List<T> soloList();

    default T single() {
        return soloList().get(0);
    }

    static <T> AdaptiveList<T> staticList(Collection<T> list) {
        return new StaticAdaptiveList<>(new CollectionDALCollection<>(list));
    }

    static <T> AdaptiveList<T> staticList(Supplier<T> supplier) {
        return new StaticAdaptiveList<>(new InfiniteDALCollection<>(supplier));
    }

    default Stream<T> stream() {
        return list().values();
    }
}
