package com.github.leeonky.dal.extensions.jdbc;

public interface Association<T extends Association<T>> {
    default Callable<T> clause() {
        return clause -> create(getQuery().clause(clause));
    }

    Query getQuery();

    T create(Query query);

    default Callable<T> through() {
        return table -> create(getQuery().through(table));
    }
}
