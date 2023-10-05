package com.github.leeonky.dal.extensions.jdbc;

import org.javalite.common.Inflector;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

public class DataBaseBuilder {
    private Function<Statement, Collection<String>> tableQuery = s -> Collections.emptyList();
    private BiFunction<String, String, String> joinColumnStrategy = (parent, child) -> Inflector.singularize(parent) + "_id";
    private BiFunction<String, String, String> referencedColumnStrategy = (parent, child) -> "id";
    private final Map<String, Map<String, Function<DataBase.Row<?>, ?>>> rowMethods = new HashMap<>();

    public DataBaseBuilder tablesProvider(Function<Statement, Collection<String>> query) {
        tableQuery = query;
        return this;
    }

    public DataBase connect(Connection connection) {
        return new DataBase(connection, this);
    }

    public Function<Statement, Collection<String>> tablesProvider() {
        return tableQuery;
    }

    public DataBaseBuilder joinColumn(BiFunction<String, String, String> joinColumn) {
        joinColumnStrategy = joinColumn;
        return this;
    }

    public DataBaseBuilder referencedColumn(BiFunction<String, String, String> referencedColumn) {
        referencedColumnStrategy = referencedColumn;
        return this;
    }

    public <T> void registerRowMethod(String table, String property, Function<DataBase.Row<?>, T> method) {
        rowMethods.computeIfAbsent(table, t -> new HashMap<>()).put(property, method);
    }

    public Optional<Function<DataBase.Row<?>, ?>> rowMethod(String table, String column) {
        return ofNullable(rowMethods.getOrDefault(table, emptyMap()).get(column));
    }

    public String joinColumn(String parent, String child) {
        return joinColumnStrategy.apply(parent, child);
    }

    public String referencedColumn(String parent, String child) {
        return referencedColumnStrategy.apply(parent, child);
    }
}
