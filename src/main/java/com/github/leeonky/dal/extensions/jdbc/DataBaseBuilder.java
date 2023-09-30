package com.github.leeonky.dal.extensions.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class DataBaseBuilder {
    private Function<Statement, Collection<String>> tableQuery = s -> Collections.emptyList();

    public DataBaseBuilder tableQuery(Function<Statement, Collection<String>> query) {
        tableQuery = query;
        return this;
    }

    public DataBase connect(Connection connection) {
        return new DataBase(connection, tableQuery);
    }
}
