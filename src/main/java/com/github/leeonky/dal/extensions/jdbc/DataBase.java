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
            return Suppressor.get(() -> query(buildSql()));
        }

        protected String buildSql() {
            return clause.buildSql(name);
        }

        @SuppressWarnings("unchecked")
        protected T createInstance(Clause clause) {
            return (T) new Table<>(name, clause);
        }

        private Iterator<DataBase.Row<T>> query(String sql) throws SQLException {
            ClauseParser parser = new ClauseParser(sql);
//            TODO refactor
            PreparedStatement preparedStatement = connection.prepareStatement(parser.getClause());
            int parameterIndex = 1;
            for (String parameter : parser.getParameters())
                preparedStatement.setObject(parameterIndex++, clause.parameters().get(parameter));
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

                @SuppressWarnings("unchecked")
                private DataBase.Row<T> getRow() throws SQLException {
                    Map<String, Object> rowData = new LinkedHashMap<>();
                    DataBase.Row<T> row = new DataBase.Row<>((T) Table.this, rowData);
                    for (String column : columns)
                        rowData.put(column, resultSet.getObject(column));
                    return row;
                }
            };
        }

        public Clause clause() {
            return clause;
        }

        public T select(String select) {
            return createInstance(clause.select(select));
        }

        public T where(String clause) {
            return createInstance(this.clause.where(clause));
        }

        public T appendParameters(Map<String, Object> parameters) {
            return createInstance(clause.parameters(parameters));
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
            return join(parentTableName).asParent().exactlyOne();
        }

        public LinkedTable join(String anotherTableName) {
            return new LinkedTable(this, anotherTableName);
        }

        public LinkedRow hasOne(String maybeChildTableName) {
            return join(maybeChildTableName).asChildren().exactlyOne();
        }

        public LinkedTable hasMany(String mayBeChildTableName) {
            return join(mayBeChildTableName).asChildren();
        }

        public T table() {
            return table;
        }
    }

    public class LinkedTable extends Table<LinkedTable> {
        protected final Row<? extends Table<?>> row;

        public LinkedTable(Row<? extends Table<?>> row, String name) {
            super(name);
            this.row = row;
            clause.parameters().putAll(row.table().clause().parameters());
            clause.parameters().putAll(row.data());
        }

        public LinkedTable(Row<? extends Table<?>> row, String name, Clause clause) {
            super(name, clause);
            this.row = row;
        }

        //        TODO rename
        public LinkedTable valueColumn(String valueColumn) {
            return createInstance(clause.defaultValueColumn(valueColumn));
        }

        public LinkedTable joinColumn(String joinColumn) {
            return createInstance(clause.defaultJoinColumn(joinColumn));
        }

        public LinkedTable on(String condition) {
            return createInstance(clause.on(condition));
        }

        @Override
        protected LinkedTable createInstance(Clause clause) {
            return new LinkedTable(row, name(), clause);
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

        public LinkedTable asParent() {
            return joinColumn(builder.referencedColumn().apply(row.table().name(), name()))
                    .valueColumn(builder.joinColumn().apply(row.table().name(), name()));
        }

        public LinkedTable asChildren() {
            return joinColumn(builder.joinColumn().apply(name(), row.table().name()))
                    .valueColumn(builder.referencedColumn().apply(name(), row.table().name()));
        }

        public LinkedTable through(String joinTableName) {
            String[] joinTableAndColumn = joinTableName.split("\\.");
            LinkedTable thoughTable = row.join(joinTableAndColumn[0]).asChildren().select(joinTableAndColumn[1]);
            return new LinkedThroughTable(this, thoughTable);
        }
    }

    public class LinkedThroughTable extends LinkedTable {
        private final LinkedTable thoughTable;
        private final String referencedColumn;

        public LinkedThroughTable(LinkedTable linkedTable, LinkedTable thoughTable) {
            super(linkedTable.row, linkedTable.name(), linkedTable.clause().on(null));
            this.thoughTable = thoughTable;
            referencedColumn = linkedTable.clause().defaultJoinColumn();
        }

        public LinkedThroughTable(LinkedTable thoughTable, String referencedColumn,
                                  Row<? extends Table<?>> row, String name, Clause clause) {
            super(row, name, clause);
            this.thoughTable = thoughTable;
            this.referencedColumn = referencedColumn;
        }

        @Override
        protected String buildSql() {
            return clause().where(String.format("%s in (%s)", referencedColumn, thoughTable.buildSql())).buildSql(name());
        }

        @Override
        protected LinkedTable createInstance(Clause clause) {
            return new LinkedThroughTable(thoughTable, referencedColumn, row, name(), clause);
        }

        @Override
        public LinkedTable on(String condition) {
            return new LinkedThroughTable(thoughTable.on(condition), referencedColumn, row, name(), clause);
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
