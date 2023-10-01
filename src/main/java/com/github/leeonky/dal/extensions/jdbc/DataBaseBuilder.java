package com.github.leeonky.dal.extensions.jdbc;

import org.javalite.common.Inflector;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataBaseBuilder {
    private Function<Statement, Collection<String>> tableQuery = s -> Collections.emptyList();
    private BiFunction<String, String, String> joinColumn = (table, joinTable) -> Inflector.singularize(joinTable) + "_id";
    private BiFunction<String, String, String> referencedColumn = (table, joinTable) -> "id";

    public DataBaseBuilder tableQuery(Function<Statement, Collection<String>> query) {
        tableQuery = query;
        return this;
    }

    public DataBase connect(Connection connection) {
        return new DataBase(connection, this);
    }

    public Function<Statement, Collection<String>> tableQuery() {
        return tableQuery;
    }

    public BiFunction<String, String, String> joinColumn() {
        return joinColumn;
    }

    public BiFunction<String, String, String> referencedColumn() {
        return referencedColumn;
    }

    public DataBaseBuilder joinColumn(BiFunction<String, String, String> joinColumn) {
        this.joinColumn = joinColumn;
        return this;
    }

    public DataBaseBuilder referencedColumn(BiFunction<String, String, String> referencedColumn) {
        this.referencedColumn = referencedColumn;
        return this;
    }
}
