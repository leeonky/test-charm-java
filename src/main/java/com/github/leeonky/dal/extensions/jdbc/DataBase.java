package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;

import java.sql.*;
import java.util.*;

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
        private Row oneRow;

        public Table(String name) {
            this(name, new Clause(name + ".*", null));
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
                    Row row = new Row();
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

        public class Row extends LinkedHashMap<String, Object> {

            @Override
            public Object get(Object key) {
                if (containsKey(key))
                    return super.get(key);
                return builder.rowMethod(name())
                        .orElseThrow(() -> new RuntimeException("No such column: " + key)).apply(this);
            }
        }
    }
}
