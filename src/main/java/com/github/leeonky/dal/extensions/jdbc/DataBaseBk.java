package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;

import java.sql.*;
import java.util.*;
import java.util.function.BiFunction;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

@Deprecated
public class DataBaseBk {
    private final Connection connection;
    private final DataBaseBuilder builder;

    DataBaseBk(Connection connection, DataBaseBuilder builder) {
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

        public String name() {
            return name;
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

            public class QueryBuilder {
                private final String table;

                public QueryBuilder(String table) {
                    this.table = table;
                }

                public Query reversJoin() {
                    return new DataBaseBk.SubQuery(Table.this, Row.this, table)
                            .defaultColumn(builder.joinColumn().apply(table, name))
                            .defaultValue(builder.referencedColumn().apply(table, name));
                }

                public Query join() {
                    return new DataBaseBk.SubQuery(Table.this, Row.this, table)
                            .defaultColumn(builder.referencedColumn().apply(name, table))
                            .defaultValue(builder.joinColumn().apply(name, table));
                }
            }

            public class OneToOne extends DataBaseBk.Table.Row implements Association<OneToOne> {
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

                @Override
                public OneToOne create(Query query) {
                    return Row.this.new OneToOne(query);
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

                @Override
                public OneToMany create(Query query) {
                    return Row.this.new OneToMany(query);
                }
            }
        }

    }

    public class SubQuery implements Query {
        private final Table mainTable;
        private final Table.Row row;
        private final String subTable;
        private String column, value, defaultColumn, defaultValue;
        private BiFunction<String, String, String> clause = (c, v) -> c + " = :" + v;

        public SubQuery(Table mainTable, Table.Row row, String subTable) {
            this.mainTable = mainTable;
            this.row = row;
            this.subTable = subTable;
        }

        public String column() {
            return column == null ? defaultColumn : column;
        }

        public String value() {
            return value == null ? defaultValue : value;
        }

        @Override
        public Query clause(String clause) {
            if (ClauseParser.onlyColumn(clause))
                column = clause;
            else if (ClauseParser.onlyParameter(clause))
                value = clause.substring(1);
            else
                this.clause = (c, v) -> clause;
            return this;
        }

        protected String clauseParser() {
            return clause.apply(column(), value());
        }

        @Override
        public List<Map<String, Object>> query() {
            ClauseParser parser = new ClauseParser(clauseParser());
            return Suppressor.get(() -> executeQuery(format("select * from %s where %s", subTable, parser.getClause()),
                    parser.getParameters().stream().map(row::get).toArray()));
        }

        @Override
        public Query through(String table) {
            String[] tableAndId = table.split("\\.");
            if (column == null)
                column = builder.referencedColumn().apply(subTable, tableAndId[0]);
            return new JoinQuery(row, tableAndId[0], tableAndId.length == 1 ? builder.joinColumn().apply(tableAndId[0], subTable) : tableAndId[1])
                    .defaultColumn(builder.joinColumn().apply(tableAndId[0], mainTable.name))
                    .defaultValue(builder.referencedColumn().apply(tableAndId[0], mainTable.name));
        }

        public SubQuery defaultColumn(String defaultColumn) {
            this.defaultColumn = defaultColumn;
            return this;
        }

        public SubQuery defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public class JoinQuery extends SubQuery {
            private final String joinId;

            public JoinQuery(Table.Row row, String joinTable, String joinId) {
                super(mainTable, row, joinTable);
                this.joinId = joinId;
            }

            @Override
            public List<Map<String, Object>> query() {
                ClauseParser parser = new ClauseParser(clauseParser());
                String sql = String.format("select %s.* from %s where %s in (select %s from %s where %s)",
                        SubQuery.this.subTable, SubQuery.this.subTable, SubQuery.this.column, joinId, super.subTable, parser.getClause());
                return Suppressor.get(() -> executeQuery(sql, parser.getParameters().stream().map(row::get).toArray()));
            }
        }
    }
}
