package org.testcharm.dal.runtime;

import java.util.function.Supplier;

public class DefaultSoloList<T> implements SoloList<T> {
    private final Supplier<DALCollection<T>> list;

    public DefaultSoloList(DALCollection<T> list) {
        this.list = () -> list;
    }

    public DefaultSoloList(Supplier<DALCollection<T>> list) {
        this.list = list;
    }

    @Override
    public DALCollection<T> list() {
        return list.get();
    }
}
