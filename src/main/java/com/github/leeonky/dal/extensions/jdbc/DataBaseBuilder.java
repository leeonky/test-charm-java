package com.github.leeonky.dal.extensions.jdbc;

import org.javalite.common.Inflector;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

public class DataBaseBuilder {
    private Function<Statement, Collection<String>> tableQuery = s -> Collections.emptyList();
    private BiFunction<String, String, String> joinColumn = (table, joinTable) -> Inflector.singularize(joinTable) + "_id";
    private BiFunction<String, String, String> referencedColumn = (table, joinTable) -> "id";
    private final Map<String, Function<DataBase.Table.Row, ?>> rowMethods = new HashMap<>();

    public DataBaseBuilder tableQuery(Function<Statement, Collection<String>> query) {
        tableQuery = query;
        return this;
    }

    public DataBase connect(Connection connection) {
        return new DataBase(connection, this);
    }

    public DataBaseBk connect2(Connection connection) {
        return new DataBaseBk(connection, this);
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

    public <T> void registerRowMethod(String table, Function<DataBase.Table.Row, T> method) {
        rowMethods.put(table, method);
    }

    public Optional<Function<DataBase.Table.Row, ?>> rowMethod(String table) {
        return ofNullable(rowMethods.get(table));
    }
}
