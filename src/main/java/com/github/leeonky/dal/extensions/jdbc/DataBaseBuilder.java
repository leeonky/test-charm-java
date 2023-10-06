package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.extensions.jdbc.DataBase.Table;
import org.javalite.common.Inflector;

import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

public class DataBaseBuilder {
    private Function<Statement, Collection<String>> tableQuery = s -> Collections.emptyList();
    private final Map<String, TableStrategy> tableStrategies = new LinkedHashMap<>();
    private final TableStrategy defaultStrategy = new TableStrategy();

    {
        defaultStrategy.joinColumnStrategy((parent, child) -> Inflector.singularize(parent.name()) + "_id");
        defaultStrategy.referencedColumnStrategy((parent, child) -> "id");
    }

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

    public TableStrategy defaultStrategy() {
        return defaultStrategy;
    }

    public String resolveJoinColumn(Table<?> parent, Table<?> child) {
        return tableStrategy(parent.name()).resolveJoinColumn(parent, child);
    }

    public String resolveReferencedColumn(Table<?> parent, Table<?> child) {
        return tableStrategy(parent.name()).resolveReferencedColumn(parent, child);
    }

    public <R> R callRowMethod(DataBase.Row<?> row, String column) {
        return tableStrategy(row.table().name()).callRowMethod(row, column);
    }

    public TableStrategy tableStrategy(String table) {
        return tableStrategies.computeIfAbsent(table, k -> new TableStrategy());
    }

    public <R> R callTableMethod(Table<?> table, String methodName) {
        return tableStrategy(table.name()).callTableMethod(table, methodName);
    }

    public <T> DataBaseBuilder registerRowMethod(String name, Function<DataBase.Row<?>, T> method) {
        defaultStrategy.registerRowMethod(name, method);
        return this;
    }

    public <R> DataBaseBuilder registerMethod(String name, Function<Table<?>, R> method) {
        defaultStrategy.registerMethod(name, method);
        return this;
    }

    public class TableStrategy {
        private final Map<String, BiFunction<Table<?>, Table<?>, String>> joinColumnStrategies = new LinkedHashMap<>();
        private final Map<String, BiFunction<Table<?>, Table<?>, String>> referencedColumnStrategies = new LinkedHashMap<>();

        private BiFunction<Table<?>, Table<?>, String> joinColumnStrategy;
        private BiFunction<Table<?>, Table<?>, String> referencedColumnStrategy;
        private final Map<String, Function<DataBase.Row<?>, ?>> rowMethods = new LinkedHashMap<>();
        private final Map<String, Function<DataBase.Table<?>, ?>> tableMethods = new LinkedHashMap<>();

        public TableStrategy joinColumnStrategy(String table, BiFunction<Table<?>, Table<?>, String> strategy) {
            joinColumnStrategies.put(table, strategy);
            return this;
        }

        public TableStrategy joinColumnStrategy(BiFunction<Table<?>, Table<?>, String> strategy) {
            joinColumnStrategy = strategy;
            return this;
        }

        public TableStrategy referencedColumnStrategy(String table, BiFunction<Table<?>, Table<?>, String> strategy) {
            referencedColumnStrategies.put(table, strategy);
            return this;
        }

        public TableStrategy referencedColumnStrategy(BiFunction<Table<?>, Table<?>, String> strategy) {
            referencedColumnStrategy = strategy;
            return this;
        }

        public <T> TableStrategy registerRowMethod(String name, Function<DataBase.Row<?>, T> method) {
            rowMethods.put(name, method);
            return this;
        }

        public <R> TableStrategy registerMethod(String name, Function<Table<?>, R> method) {
            tableMethods.put(name, method);
            return this;
        }

        public String resolveJoinColumn(Table<?> parent, Table<?> child) {
            return ofNullable(joinColumnStrategies.getOrDefault(child.name(), joinColumnStrategy))
                    .orElse(defaultStrategy.joinColumnStrategy).apply(parent, child);
        }

        public String resolveReferencedColumn(Table<?> parent, Table<?> child) {
            return ofNullable(referencedColumnStrategies.getOrDefault(child.name(), referencedColumnStrategy))
                    .orElse(defaultStrategy.referencedColumnStrategy).apply(parent, child);
        }

        @SuppressWarnings("unchecked")
        public <R> R callRowMethod(DataBase.Row<?> row, String column) {
            return (R) query(rowMethods, defaultStrategy.rowMethods, column)
                    .orElseThrow(() -> new RuntimeException("No such column: " + column))
                    .apply(row);
        }

        @SuppressWarnings("unchecked")
        public <R> R callTableMethod(Table<?> table, String methodName) {
            return (R) query(tableMethods, defaultStrategy.tableMethods, table.name())
                    .orElseThrow(() -> new RuntimeException("No such table method: " + methodName))
                    .apply(table);
        }

        private <T> Optional<T> query(Map<String, T> map1, Map<String, T> map2, String key) {
            T t = map1.get(key);
            if (t == null)
                t = map2.get(key);
            return ofNullable(t);
        }
    }
}
