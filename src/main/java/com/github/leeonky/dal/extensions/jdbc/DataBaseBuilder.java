package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.extensions.jdbc.DataBase.Table;
import org.javalite.common.Inflector;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Collections.emptyMap;

public class DataBaseBuilder {
    private Function<Statement, Collection<String>> tableQuery = s -> Collections.emptyList();
    private BiFunction<Table<?>, Table<?>, String> joinColumnStrategy = (parent, child) -> Inflector.singularize(parent.name()) + "_id";
    private BiFunction<Table<?>, Table<?>, String> referencedColumnStrategy = (parent, child) -> "id";
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
    public DataBaseBuilder joinColumnStrategy(BiFunction<Table<?>, Table<?>, String> strategy) {
        joinColumnStrategy = strategy;
        return this;
    }

    //    TODO improve parent, method chain register(table).referencedColumnStrategy()
    //    TODO improve parent, method chain register(table).referencedColumnStrategy(child)
    public DataBaseBuilder referencedColumnStrategy(BiFunction<Table<?>, Table<?>, String> strategy) {
        referencedColumnStrategy = strategy;
        return this;
    }

    //    TODO improve parent, method chain register(table).method(property, method);
    public <T> void registerRowMethod(String table, String property, Function<DataBase.Row<?>, T> method) {
        rowMethods.computeIfAbsent(table, t -> new HashMap<>()).put(property, method);
    }

    public String resolveJoinColumn(Table<?> parent, Table<?> child) {
        return joinColumnStrategy.apply(parent, child);
    }

    public String resolveReferencedColumn(Table<?> parent, Table<?> child) {
        return referencedColumnStrategy.apply(parent, child);
    }

    @SuppressWarnings("unchecked")
    public <R> R callRowMethod(DataBase.Row<?> row, String column) {
        Function<DataBase.Row<?>, ?> rowFunction = rowMethods.getOrDefault(row.table.name(), emptyMap()).get(column);
        if (rowFunction == null)
            throw new RuntimeException("No such column: " + column);
        return (R) rowFunction.apply(row);
    }
}
