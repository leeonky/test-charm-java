package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;
import org.javalite.common.Inflector;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataBase {
    private final Connection connection;
    private final Function<Statement, Collection<String>> tableQuery;

    DataBase(Connection connection, Function<Statement, Collection<String>> tableQuery) {
        this.connection = connection;
        this.tableQuery = tableQuery;
    }

    public Collection<String> allTableNames() {
        return tableQuery.apply(Suppressor.get(connection::createStatement));
    }

    public Table table(String name) {
        return new Table(name);
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
                return table -> new BelongsTo(table, String.format(":%s = id", Inflector.singularize(table) + "_id"));
            }

            public class BelongsTo {
                private final String table;
                private final Clause clause;
                private Map<String, Object> data;

                public BelongsTo(String table, String clause) {
                    this.table = table;
                    this.clause = new Clause(clause);
                }

                public Callable<BelongsTo> clause() {
                    return clause -> new BelongsTo(table, clause);
                }

                private void query() {
                    if (data == null) {
                        List<Map<String, Object>> result = querySubObject();
                        if (result.size() == 1)
                            data = result.get(0);
                        else if (result.size() > 1)
                            throw new RuntimeException("Query more than one record");
                    }
                }

                private List<Map<String, Object>> querySubObject() {
                    if (clause.onlyColumn())
                        return Suppressor.get(() -> executeQuery(String.format("select * from %s where ? = %s", table, clause.getClause()),
                                LinkedHashMap::new, get(Inflector.singularize(table) + "_id")));
                    else if (clause.onlyParameter())
                        return Suppressor.get(() -> executeQuery(String.format("select * from %s where %s = id", table, clause.getClause()),
                                LinkedHashMap::new, clause.getParameters().stream().map(Row.this::get).toArray()));
                    else
                        return Suppressor.get(() -> executeQuery(String.format("select * from %s where %s", table, clause.getClause()),
                                LinkedHashMap::new, clause.getParameters().stream().map(Row.this::get).toArray()));
                }

                public Object getValue(String column) {
                    query();
                    return data.get(column);
                }

                public Set<String> keys() {
                    query();
                    return data.keySet();
                }

                public boolean hasData() {
                    query();
                    return data != null;
                }
            }
        }
    }
}
