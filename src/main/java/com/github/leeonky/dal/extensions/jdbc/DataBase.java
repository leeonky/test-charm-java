package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;

import java.sql.*;
import java.util.*;
import java.util.function.Supplier;

public class DataBase {
    public final Connection connection;
    public final DataBaseBuilder builder;

    public DataBase(Connection connection, DataBaseBuilder builder) {
        this.connection = connection;
        this.builder = builder;
    }

    public Collection<String> allTableNames() {
        return builder.tableQuery().apply(Suppressor.get(connection::createStatement));
    }

    public Table table(String name) {
        return new Table(name);
    }

    public class Table implements Iterable<Table.Row> {
        private final String name;
        private final Clause clause;

        public Table(String name) {
            this(name, new Clause(name + ".*"));
        }

        private Table(String name, Clause clause) {
            this.name = name;
            this.clause = clause;
        }

        public String name() {
            return name;
        }

        @Override
        public Iterator<Row> iterator() {
            return Suppressor.get(() -> query(clause.buildSql(name)));
        }

        public Table where(String clause) {
            return new Table(name, this.clause.where(clause));
        }

        public Table on(String condition) {
            return new Table(name, clause.on(condition));
        }

        public Table appendParameters(Map<String, Object> parameters) {
            return new Table(name, clause.parameters(parameters));
        }

        public Table select(String select) {
            return new Table(name, clause.select(select));
        }

        private Iterator<Row> query(String sql) throws SQLException {
            ClauseParser parser = new ClauseParser(sql);
            PreparedStatement preparedStatement = connection.prepareStatement(parser.getClause());
            int parameterIndex = 1;
            for (String parameter : parser.getParameters())
                preparedStatement.setObject(parameterIndex++, clause.parameters.get(parameter));
            ResultSet resultSet = preparedStatement.executeQuery();
            return new Iterator<Row>() {
                private final Set<String> columns = new LinkedHashSet<String>() {{
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; ++i)
                        add(metaData.getColumnLabel(i).toLowerCase());
                }};

                @Override
                public boolean hasNext() {
                    return Suppressor.get(resultSet::next);
                }

                @Override
                public Row next() {
                    return Suppressor.get(this::getRow);
                }

                private Row getRow() throws SQLException {
                    Map<String, Object> rowData = new LinkedHashMap<>();
                    Row row = new Row(rowData);
                    for (String column : columns)
                        rowData.put(column, resultSet.getObject(column));
                    return row;
                }
            };
        }

        private Row exactlyOne() {
            return new Row(() -> {
                Iterator<Row> iterator = iterator();
                if (iterator.hasNext()) {
                    Row row = iterator.next();
                    if (iterator.hasNext())
                        throw new RuntimeException("Result set has multiple records");
                    return row.query();
                }
                return null;
            });
        }

        public class Row {
            private Map<String, Object> data;
            private Supplier<Map<String, Object>> query;

            public Row(Map<String, Object> data) {
                this.data = data;
            }

            public Row(Supplier<Map<String, Object>> query) {
                this.query = query;
            }

            public Object column(String column) {
                query();
                if (data.containsKey(column))
                    return data.get(column);
                return builder.rowMethod(name())
                        .orElseThrow(() -> new RuntimeException("No such column: " + column)).apply(this);
            }

            public Set<String> columns() {
                query();
                return data.keySet();
            }

            public Map<String, Object> query() {
                if (data == null)
                    data = query.get();
                return data;
            }

            public Row belongsTo(String parentTableName) {
                return table(parentTableName)
                        .defaultJoinColumn(builder.referencedColumn().apply(name(), parentTableName))
                        .defaultValueColumn(builder.joinColumn().apply(name(), parentTableName))
                        .appendParameters(query()).exactlyOne();
            }

            public Row where(String clause) {
                return Table.this.where(clause).exactlyOne();
            }

            public boolean empty() {
                return query() == null;
            }

            public Row on(String condition) {
                return Table.this.on(condition).exactlyOne();
            }
        }

        public Table defaultValueColumn(String valueColumn) {
            return new Table(name, clause.defaultValueColumn(valueColumn));
        }

        public Table defaultJoinColumn(String joinColumn) {
            return new Table(name, clause.defaultJoinColumn(joinColumn));
        }
    }
}
