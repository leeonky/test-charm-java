package org.testcharm.dal.runtime;

import java.util.function.Supplier;

public class StaticAdaptiveList<T> implements AdaptiveList<T> {
    private final Supplier<DALCollection<T>> list;

    public StaticAdaptiveList(DALCollection<T> list) {
        this.list = () -> list;
    }

    public StaticAdaptiveList(Supplier<DALCollection<T>> list) {
        this.list = list;
    }

    @Override
    public DALCollection<T> list() {
        return list.get();
    }
}
