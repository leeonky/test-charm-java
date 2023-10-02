package com.github.leeonky.dal.extensions.jdbc;

public interface Association<T extends Association<T>> {
    default Callable<T> clause() {
        return clause -> {
            reset();
            getQuery().clause(clause);
            return (T) this;
        };
    }

    DataBase.Table.Row.Query getQuery();

    default void reset() {
    }
}
