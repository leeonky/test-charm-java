package com.github.leeonky.dal.runtime;

import java.util.Collection;
import java.util.function.Supplier;

public class AdaptiveList<T> {
    private DALCollection<T> list;

    public AdaptiveList(Collection<T> list) {
        this.list = new CollectionDALCollection<>(list);
    }

    public AdaptiveList(Supplier<T> supplier) {
        list = new InfiniteDALCollection<>(supplier);
    }

    public DALCollection<T> list() {
        return list;
    }

    public T only() {
        if (list.size() != 1)
            throw new IllegalStateException("Expected only one element");
        return list.getByIndex(list.firstIndex());
    }

    public static <T> AdaptiveList<T> staticList(Collection<T> list) {
        return new AdaptiveList<>(list);
    }

    public static <T> AdaptiveList<T> staticList(Supplier<T> supplier) {
        return new AdaptiveList<>(supplier);
    }
}
