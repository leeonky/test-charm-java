package com.github.leeonky.dal.extensions.jdbc;

public interface CanWhere<T extends CanWhere<T>> {
    T where(String clause);
}
