package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;
import org.javalite.common.Inflector;

import java.sql.*;
import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

public class DataBase {
    private final Connection connection;
    private final DataBaseBuilder builder;

    DataBase(Connection connection, DataBaseBuilder builder) {
        this.connection = connection;
        this.builder = builder;
    }

    public Collection<String> allTableNames() {
        return builder.tableQuery().apply(Suppressor.get(connection::createStatement));
    }

    public Table table(String name) {
        return new Table(name);
    }

    private List<Map<String, Object>> executeQuery(String sql, Object... params)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++)
            preparedStatement.setObject(i + 1, params[i]);
        ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<Map<String, Object>> result = new ArrayList<>();
        while (resultSet.next()) {
            int columnCount = metaData.getColumnCount();
            Map<String, Object> row = new LinkedHashMap<>();
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
            return Suppressor.get(() -> executeQuery("select * from " + name)).stream().map(Row::new).iterator();
        }

        public class Row {

            protected Map<String, Object> data;

            public Row(Map<String, Object> data) {
                this.data = data;
            }

            public Callable<BelongsTo> callBelongsTo() {
                return table -> new BelongsTo(table, format("%s = :%s",
                        builder.referencedColumn().apply(name, table),
                        builder.joinColumn().apply(name, table)));
            }

            public Callable<HasMany> callHasMany() {
                return table -> new HasMany(table, format("%s = :%s",
                        builder.joinColumn().apply(table, name),
                        builder.referencedColumn().apply(table, name)
                ));
            }

            public Callable<HasOne> callHasOne() {
                return table -> new HasOne(table, format("%s = :%s",
                        builder.joinColumn().apply(table, name),
                        builder.referencedColumn().apply(table, name)
                ));
            }

            public Object get(String key) {
                lazyQuery();
                return data.get(key);
            }

            protected void lazyQuery() {
                if (data == null) {
                    List<Map<String, Object>> result = query();
                    if (result.size() == 1)
                        data = result.get(0);
                    else if (result.size() > 1)
                        throw new RuntimeException("Query more than one record");
                }
            }

            protected List<Map<String, Object>> query() {
                return emptyList();
            }

            public Set<String> columns() {
                lazyQuery();
                return data.keySet();
            }

            public boolean hasData() {
                lazyQuery();
                return data != null;
            }

            public abstract class Association extends DataBase.Table.Row {
                protected final String table;
                protected Clause clause;

                public Association(String table, String clause) {
                    super(null);
                    this.table = table;
                    this.clause = new Clause(clause);
                }

                public Callable<Association> clause() {
                    return clause -> {
                        data = null;
                        this.clause = new Clause(clause);
                        return this;
                    };
                }
            }

            public class BelongsTo extends Association {
                public BelongsTo(String table, String clause) {
                    super(table, clause);
                }

                @Override
                protected List<Map<String, Object>> query() {
                    if (clause.onlyColumn())
                        return Suppressor.get(() -> executeQuery(format("select * from %s where ? = %s", table, clause.getClause()),
                                Row.this.get(Inflector.singularize(table) + "_id")));
                    else if (clause.onlyParameter())
                        return Suppressor.get(() -> executeQuery(format("select * from %s where %s = id", table, clause.getClause()),
                                clause.getParameters().stream().map(Row.this::get).toArray()));
                    else
                        return Suppressor.get(() -> executeQuery(format("select * from %s where %s", table, clause.getClause()),
                                clause.getParameters().stream().map(Row.this::get).toArray()));
                }
            }

            public class HasOne extends Association {
                public HasOne(String table, String clause) {
                    super(table, clause);
                }

                @Override
                protected List<Map<String, Object>> query() {
                    if (clause.onlyParameter())
                        return Suppressor.get(() -> executeQuery(format("select * from %s where %s = ?", table, Inflector.singularize(name) + "_id"),
                                clause.getParameters().stream().map(Row.this::get).toArray()));
                    if (clause.onlyColumn())
                        return Suppressor.get(() -> executeQuery(format("select * from %s where ? = %s", table, clause.getClause()),
                                Row.this.get("id")));
                    return Suppressor.get(() -> executeQuery(format("select * from %s where %s", table, clause.getClause()),
                            clause.getParameters().stream().map(Row.this::get).toArray()));
                }
            }

            public class HasMany implements Iterable<Row> {
                private final String table;
                private final Clause clause;

                public HasMany(String table, String clause) {
                    this.clause = new Clause(clause);
                    this.table = table;
                }

                @Override
                public Iterator<Row> iterator() {
                    if (clause.onlyParameter())
                        return Suppressor.get(() -> executeQuery(format("select * from %s where %s = ?", table, Inflector.singularize(name) + "_id"),
                                clause.getParameters().stream().map(Row.this::get).toArray())).stream().map(Row::new).iterator();
                    if (clause.onlyColumn())
                        return Suppressor.get(() -> executeQuery(format("select * from %s where ? = %s", table, clause.getClause()),
                                get("id"))).stream().map(Row::new).iterator();
                    return Suppressor.get(() -> executeQuery(format("select * from %s where %s", table, clause.getClause()),
                            clause.getParameters().stream().map(Row.this::get).toArray())).stream().map(Row::new).iterator();
                }

                public Callable<HasMany> clause() {
                    return clause -> new HasMany(table, clause);
                }
            }
        }
    }
}
