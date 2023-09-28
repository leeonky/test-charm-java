package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
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

    private <T extends Map<String, Object>> List<T> executeQuery(String sql, Supplier<T> rowFactory, Object... params)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++)
            preparedStatement.setObject(i + 1, params[i]);
        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            int columnCount = metaData.getColumnCount();
            T row = rowFactory.get();
            for (int i = 1; i <= columnCount; ++i)
                row.put(metaData.getColumnName(i).toLowerCase(), resultSet.getObject(i));
            result.add(row);
        }
        return result;
    }

    public class Table implements Iterable<Table.Row> {
        private final String name;

        public Table(String name) {
            this.name = name;
        }

        @Override
        public Iterator<Row> iterator() {
            return Suppressor.get(() -> executeQuery("select * from " + name, Row::new)).iterator();
        }

        public class Row extends LinkedHashMap<String, Object> {
            public Callable<BelongsTo> callBelongsTo() {
//TODO default clause
                return table -> {
                    if (table.contains("@"))
                        return new BelongsTo(table.substring(0, table.indexOf('@')), String.format(":%s = id", table.substring(table.indexOf('@') + 1)));
                    return new BelongsTo(table, ":product_id = id");
                };
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
//TODO hardcode
//TODO hardcode
                    List<Map<String, Object>> result = executeQuery("select * from " + table + " where " + clause.replace(":product_id", "?"),
                            LinkedHashMap::new, get("product_id"));
                    if (result.size() > 0) {
                        data = result.get(0);
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
