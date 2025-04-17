package com.github.leeonky.dal.runtime;

public interface RuntimeDataHandler<R extends RuntimeData<?>> extends RuntimeHandler<R> {
    @Override
    Data<?> handleData(R r);

    @Override
    default Object handle(R r) {
        return null;
    }
}
