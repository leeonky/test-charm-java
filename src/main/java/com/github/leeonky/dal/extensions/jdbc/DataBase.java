package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataBase {
    private final Connection connection;
    private final Function<Statement, Collection<String>> tableQuerier;
    private final Function<Statement, Collection<String>> viewQuerier;

    DataBase(Connection connection, Function<Statement, Collection<String>> tableQuerier,
             Function<Statement, Collection<String>> viewQuerier) {
        this.connection = connection;
        this.tableQuerier = tableQuerier;
        this.viewQuerier = viewQuerier;
    }

    public Map<String, Table> getTables() {
        return Suppressor.get(() -> tableQuerier.apply(connection.createStatement())).stream()
                .collect(Collectors.toMap(Function.identity(), Table::new));
    }

    public Map<String, Table> getViews() {
        return Suppressor.get(() -> viewQuerier.apply(connection.createStatement())).stream()
                .collect(Collectors.toMap(Function.identity(), Table::new));
    }

    public class Table implements Iterable<Table.Row> {
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
                        row.put(metaData.getColumnName(i).toLowerCase(), resultSet.getObject(i));
                    return row;
                }
            };
        }

        public class Row extends LinkedHashMap<String, Object> {
            public Callable<BelongsTo> callBelongsTo() {
                return table -> new BelongsTo(table, "");
            }

            public class BelongsTo {
                private final String table;
                private final String clause;
                private Map<String, Object> data;

                public BelongsTo(String table, String clause) {
                    this.table = table;
                    this.clause = clause;
                }

                public Callable<BelongsTo> clause() {
                    return clause -> new BelongsTo(table, clause);
                }

                public void query() {
                    if (data == null) {
                        Suppressor.run(this::queryDB);
                    }
                }

                private void queryDB() throws SQLException {
                    PreparedStatement preparedStatement = connection.prepareStatement("select * from " + table + " where " + clause.replace(":product_id", "?"));
                    preparedStatement.setObject(1, get("product_id"));
                    ResultSet resultSet = preparedStatement.executeQuery();
                    ResultSetMetaData metaData = resultSet.getMetaData();

                    if (resultSet.next()) {
                        data = new LinkedHashMap<>();

                        int columnCount = metaData.getColumnCount();
                        for (int i = 1; i <= columnCount; ++i)
                            data.put(metaData.getColumnName(i).toLowerCase(), resultSet.getObject(i));
                    }
                }

                public Object getValue(String column) {
                    return data.get(column);
                }

                public Set<String> keys() {
                    return data.keySet();
                }
            }
        }
    }
}
