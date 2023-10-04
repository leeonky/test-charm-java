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

    public Table<?> table(String name) {
        return new Table<>(name);
    }

    public class Table<T extends Table<T>> implements Iterable<Row<T>> {
        private final String name;
        protected final Clause clause;

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
        public Iterator<DataBase.Row<T>> iterator() {
            return Suppressor.get(() -> query(clause.buildSql(name)));
        }

        protected T createInstance(String name, Clause clause) {
            return (T) new Table(name, clause);
        }

        private Iterator<DataBase.Row<T>> query(String sql) throws SQLException {
            ClauseParser parser = new ClauseParser(sql);
            PreparedStatement preparedStatement = connection.prepareStatement(parser.getClause());
            int parameterIndex = 1;
            for (String parameter : parser.getParameters())
                preparedStatement.setObject(parameterIndex++, clause.parameters.get(parameter));
            ResultSet resultSet = preparedStatement.executeQuery();
            return new Iterator<DataBase.Row<T>>() {
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
                public DataBase.Row<T> next() {
                    return Suppressor.get(this::getRow);
                }

                private DataBase.Row<T> getRow() throws SQLException {
                    Map<String, Object> rowData = new LinkedHashMap<>();
                    DataBase.Row<T> row = new DataBase.Row<>((T) Table.this, rowData);
                    for (String column : columns)
                        rowData.put(column, resultSet.getObject(column));
                    return row;
                }
            };
        }

        public T select(String select) {
            return createInstance(name, clause.select(select));
        }

        public T where(String clause) {
            return createInstance(name, this.clause.where(clause));
        }

        public T appendParameters(Map<String, Object> parameters) {
            return createInstance(name, clause.parameters(parameters));
        }

    }

    public class Row<T extends Table<T>> {
        protected final T table;
        protected Map<String, Object> data;

        public Row(T table, Map<String, Object> data) {
            this.table = table;
            this.data = data;
        }

        public Object column(String column) {
            if (data().containsKey(column))
                return data().get(column);
            return builder.rowMethod(table.name())
                    .orElseThrow(() -> new RuntimeException("No such column: " + column)).apply(this);
        }

        public Set<String> columns() {
            return data().keySet();
        }

        public Map<String, Object> data() {
            return data;
        }

        public boolean empty() {
            return data() == null;
        }

        public LinkedRow belongsTo(String parentTableName) {
            return new LinkedTable(parentTableName)
                    .defaultJoinColumn(builder.referencedColumn().apply(table.name(), parentTableName))
                    .defaultValueColumn(builder.joinColumn().apply(table.name(), parentTableName))
                    .appendParameters(data).exactlyOne();
        }
    }

    public class LinkedTable extends Table<LinkedTable> {
        public LinkedTable(String name) {
            super(name);
        }

        public LinkedTable(String name, Clause clause) {
            super(name, clause);
        }

        public LinkedTable defaultValueColumn(String valueColumn) {
            return new LinkedTable(name(), clause.defaultValueColumn(valueColumn));
        }

        public LinkedTable defaultJoinColumn(String joinColumn) {
            return new LinkedTable(name(), clause.defaultJoinColumn(joinColumn));
        }

        public LinkedTable on(String condition) {
            return new LinkedTable(name(), clause.on(condition));
        }

        @Override
        protected LinkedTable createInstance(String name, Clause clause) {
            return new LinkedTable(name, clause);
        }

        public DataBase.LinkedRow exactlyOne() {
            return new DataBase.LinkedRow(this, () -> {
                Iterator<Row<LinkedTable>> iterator = iterator();
                if (iterator.hasNext()) {
                    DataBase.Row<LinkedTable> row = iterator.next();
                    if (iterator.hasNext())
                        throw new RuntimeException("Result set has multiple records");
                    return row.data();
                }
                return null;
            });
        }
    }

    public class LinkedRow extends Row<LinkedTable> {
        protected Supplier<Map<String, Object>> query;

        public LinkedRow(LinkedTable table, Supplier<Map<String, Object>> query) {
            super(table, null);
            this.query = query;
        }

        private void query() {
            if (data == null)
                data = query.get();
        }

        @Override
        public Map<String, Object> data() {
            query();
            return super.data();
        }

        public LinkedRow where(String clause) {
            return table.where(clause).exactlyOne();
        }

        public LinkedRow on(String condition) {
            return table.on(condition).exactlyOne();
        }
    }
}
