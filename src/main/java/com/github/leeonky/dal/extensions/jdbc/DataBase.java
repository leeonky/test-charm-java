package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;

import java.sql.*;
import java.util.*;
import java.util.function.BiFunction;

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

            public Callable<OneToOne> callBelongsTo() {
                return table -> new OneToOne(new QueryBuilder(table).join());
            }

            public Callable<OneToMany> callHasMany() {
                return table -> new OneToMany(new QueryBuilder(table).reversJoin());
            }

            public Callable<OneToOne> callHasOne() {
                return table -> new OneToOne(new QueryBuilder(table).reversJoin());
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

            public void reset() {
                data = null;
            }

            public class QueryBuilder {
                private final String table;

                public QueryBuilder(String table) {
                    this.table = table;
                }

                public Query reversJoin() {
                    return Row.this.new Query(table)
                            .column(builder.joinColumn().apply(table, name))
                            .value(builder.referencedColumn().apply(table, name));
                }

                public Query join() {
                    return Row.this.new Query(table)
                            .column(builder.referencedColumn().apply(name, table))
                            .value(builder.joinColumn().apply(name, table));
                }
            }

            public class Query {
                private final String table;
                private String column, value;
                private BiFunction<String, String, String> clause = (c, v) -> c + " = :" + v;

                public Query(String table) {
                    this.table = table;
                }

                public Query column(String column) {
                    this.column = column;
                    return this;
                }

                public Query value(String value) {
                    this.value = value;
                    return this;
                }

                public void clause(String clause) {
                    if (ClauseParser.onlyColumn(clause))
                        column(clause);
                    else if (ClauseParser.onlyParameter(clause))
                        value(clause.substring(1));
                    else
                        this.clause = (c, v) -> clause;
                }

                public List<Map<String, Object>> query() {
                    ClauseParser parser = new ClauseParser(clause.apply(column, value));
                    return Suppressor.get(() -> executeQuery(format("select * from %s where %s", table, parser.getClause()),
                            parser.getParameters().stream().map(Row.this::get).toArray()));
                }
            }

            public class OneToOne extends DataBase.Table.Row implements Association<OneToOne> {
                private final Query query;

                public OneToOne(Query query) {
                    super(null);
                    this.query = query;
                }

                @Override
                public Query getQuery() {
                    return query;
                }

                @Override
                protected List<Map<String, Object>> query() {
                    return query.query();
                }
            }

            public class OneToMany implements Iterable<Row>, Association<OneToMany> {
                private final Query query;

                @Override
                public Query getQuery() {
                    return query;
                }

                public OneToMany(Query query) {
                    this.query = query;
                }

                @Override
                public Iterator<Row> iterator() {
                    return query.query().stream().map(Row::new).iterator();
                }
            }
        }
    }
}
