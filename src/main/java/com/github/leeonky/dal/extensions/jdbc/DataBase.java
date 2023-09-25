package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;

import java.sql.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataBase {
    private final Connection connection;
    private final Function<Statement, Collection<String>> tableQuerier;
    private final Function<Statement, Collection<String>> viewQuerier;
    private final Map<String, Table> tables, views;

    DataBase(Connection connection, Function<Statement, Collection<String>> tableQuerier,
             Function<Statement, Collection<String>> viewQuerier) {
        this.connection = connection;
        this.tableQuerier = tableQuerier;
        this.viewQuerier = viewQuerier;
        tables = Suppressor.get(() -> this.tableQuerier.apply(this.connection.createStatement())).stream()
                .collect(Collectors.toMap(Function.identity(), Table::new));
        views = Suppressor.get(() -> this.viewQuerier.apply(this.connection.createStatement())).stream()
                .collect(Collectors.toMap(Function.identity(), Table::new));
    }

    public Map<String, Table> getTables() {
        return tables;
    }

    public Map<String, Table> getViews() {
        return views;
    }

    public class Table implements Iterable<Row> {
        private final String name;

        public Table(String name) {
            this.name = name;
        }

        @Override
        public Iterator<Row> iterator() {
            return Suppressor.get(this::build);
        }

        private Iterator<Row> build() throws SQLException {
            ResultSet resultSet = connection.createStatement().executeQuery("select * from " + name);
            ResultSetMetaData metaData = resultSet.getMetaData();
            return new Iterator<Row>() {
                @Override
                public boolean hasNext() {
                    return Suppressor.get(resultSet::next);
                }

                @Override
                public Row next() {
                    return Suppressor.get(this::buildRow);
                }

                private Row buildRow() throws SQLException {
                    int columnCount = metaData.getColumnCount();
                    Row row = new Row();
                    for (int i = 1; i <= columnCount; ++i)
                        row.put(metaData.getColumnName(i), resultSet.getObject(i));
                    return row;
                }
            };
        }
    }

    public class Row extends LinkedHashMap<String, Object> {
    }
}
