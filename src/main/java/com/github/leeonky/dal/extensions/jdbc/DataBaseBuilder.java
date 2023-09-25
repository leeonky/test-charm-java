package com.github.leeonky.dal.extensions.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class DataBaseBuilder {
    private Function<Statement, Collection<String>> tableQuerier = s -> Collections.emptyList();
    private Function<Statement, Collection<String>> viewQuerier = s -> Collections.emptyList();

    public DataBaseBuilder tableQuerier(Function<Statement, Collection<String>> querier) {
        tableQuerier = querier;
        return this;
    }

    public DataBase connect(Connection connection) {
        return new DataBase(connection, tableQuerier, viewQuerier);
    }

    public DataBaseBuilder viewQuerier(Function<Statement, Collection<String>> querier) {
        viewQuerier = querier;
        return this;
    }
}
