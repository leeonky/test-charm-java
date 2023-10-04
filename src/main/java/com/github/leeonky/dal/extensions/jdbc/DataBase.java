package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;

import java.sql.*;
import java.util.*;

public class DataBase {
    private final Connection connection;
    private final DataBaseBuilder builder;

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

    public class Table implements Iterable<Row> {
        private final String name;
        private String select;
        private String clause;
        private final Map<String, Object> parameters = new HashMap<>();
        private Row oneRow;

        public Table(String name) {
            this.name = name;
            select = name + ".*";
        }

        public String name() {
            return name;
        }

        @Override
        public Iterator<DataBase.Row> iterator() {
            return Suppressor.get(() -> {
                StringBuilder sql = new StringBuilder().append("select ").append(select).append(" from ").append(name);
                if (clause != null) {
                    sql.append(" where ");
                    sql.append(clause);
                }
                return query(sql.toString());
            });
        }

        public Table where(String clause) {
            this.clause = clause;
            return this;
        }

        public Table appendParameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }

        public Table select(String select) {
            this.select = select;
            return this;
        }

        private Iterator<DataBase.Row> query(String sql) throws SQLException {
            ClauseParser parser = new ClauseParser(sql);
            PreparedStatement preparedStatement = connection.prepareStatement(parser.getClause());
            int parameterIndex = 1;
            for (String parameter : parser.getParameters())
                preparedStatement.setObject(parameterIndex++, parameters.get(parameter));
            ResultSet resultSet = preparedStatement.executeQuery();
            return new Iterator<DataBase.Row>() {
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
                public DataBase.Row next() {
                    return Suppressor.get(this::getRow);
                }

                private DataBase.Row getRow() throws SQLException {
                    Row row = new Row(Table.this);
                    for (String column : columns)
                        row.put(column, resultSet.getObject(column));
                    return row;
                }
            };
        }

        private void queryOneRow() {
            if (oneRow == null) {
                Iterator<Row> iterator = iterator();
                if (iterator.hasNext()) {
                    oneRow = iterator.next();
                    if (iterator.hasNext())
                        throw new RuntimeException("Result set has more than one rows");
                } else
                    throw new RuntimeException("Result set is empty");
            }
        }

        public Set<String> oneRowColumnNames() {
            queryOneRow();
            return oneRow.keySet();
        }

        public Object oneRowColumn(String property) {
            queryOneRow();
            return oneRow.get(property);
        }
    }

    public class Row extends LinkedHashMap<String, Object> {
        private final Table table;

        public Row(Table table) {
            this.table = table;
        }

        @Override
        public Object get(Object key) {
            if (containsKey(key))
                return super.get(key);
            return builder.rowMethod(table.name())
                    .orElseThrow(() -> new RuntimeException("No such column: " + key)).apply(this);
        }
    }
}
