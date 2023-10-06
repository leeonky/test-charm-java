package com.github.leeonky.dal.extensions.jdbc;

public interface Association<T> {
    T through(String table, String joinColumn);

    T through(String table);

    default T throughWithColumn(String joinTableName) {
        String[] joinTableAndColumn = joinTableName.split("\\.");
        return joinTableAndColumn.length > 1 ? through(joinTableAndColumn[0], joinTableAndColumn[1])
                : through(joinTableAndColumn[0]);
    }

    T on(String condition);
}
