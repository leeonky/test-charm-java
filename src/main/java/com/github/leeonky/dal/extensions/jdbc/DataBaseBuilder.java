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

    //    TODO improve parent, method chain register(table).joinColumnStrategy()
    //    TODO improve parent, method chain register(table).joinColumnStrategy(child)
    public DataBaseBuilder joinColumnStrategy(BiFunction<String, String, String> strategy) {
        joinColumnStrategy = strategy;
        return this;
    }

    //    TODO improve parent, method chain register(table).referencedColumnStrategy()
    //    TODO improve parent, method chain register(table).referencedColumnStrategy(child)
    public DataBaseBuilder referencedColumnStrategy(BiFunction<String, String, String> strategy) {
        referencedColumnStrategy = strategy;
        return this;
    }

    //    TODO improve parent, method chain register(table).method(property, method);
    public <T> void registerRowMethod(String table, String property, Function<DataBase.Row<?>, T> method) {
        rowMethods.computeIfAbsent(table, t -> new HashMap<>()).put(property, method);
    }

    public Optional<Function<DataBase.Row<?>, ?>> rowMethod(String table, String column) {
        return ofNullable(rowMethods.getOrDefault(table, emptyMap()).get(column));
    }

    public String joinColumnStrategy(String parent, String child) {
        return joinColumnStrategy.apply(parent, child);
    }

    public String referencedColumnStrategy(String parent, String child) {
        return referencedColumnStrategy.apply(parent, child);
    }
}
