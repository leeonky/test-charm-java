package com.github.leeonky.dal.runtime;

import java.util.Collection;

public class AdaptiveList<T> {
    private DALCollection<T> list;

    public AdaptiveList(Collection<T> list) {
        this.list = new CollectionDALCollection<>(list);
    }

    public DALCollection<T> list() {
        return list;
    }

    public T only() {
        if (list.size() != 1)
            throw new IllegalStateException("Expected only one element");
        return list.getByIndex(list.firstIndex());
    }
}
