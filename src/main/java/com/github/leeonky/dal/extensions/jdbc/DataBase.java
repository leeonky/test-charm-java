package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;

import java.sql.*;
import java.util.*;
import java.util.function.Supplier;

public class DataBase {
    public final Connection connection;
    public final DataBaseBuilder builder;
    public Set<String> queriedTables = new LinkedHashSet<>();

    public DataBase(Connection connection, DataBaseBuilder builder) {
        this.connection = connection;
        this.builder = builder;
    }

    public Collection<String> allTableNames() {
        return new LinkedHashSet<String>() {{
            addAll(builder.tablesProvider().apply(Suppressor.get(connection::createStatement)));
            addAll(queriedTables);
        }};
    }

    public Table<?> table(String name) {
        queriedTables.add(name);
        return new Table<>(name);
    }

    public class Table<T extends Table<T>> implements Iterable<Row<T>> {
        private final String name;
        protected final Query query;

        public Table(String name) {
            this(name, new Query(name, name + ".*"));
        }

        private Table(String name, Query query) {
            this.name = name;
            this.query = query;
        }

        public String name() {
            return name;
        }

        @Override
        public Iterator<DataBase.Row<T>> iterator() {
            return Suppressor.get(() -> query(buildSql()));
        }

        protected String buildSql() {
            return query.buildSql();
        }

        @SuppressWarnings("unchecked")
        protected T createInstance(Query query) {
            return (T) new Table<>(name, query);
        }

        private Iterator<DataBase.Row<T>> query(String sql) throws SQLException {
            ClauseParser parser = new ClauseParser(sql);
            PreparedStatement preparedStatement = connection.prepareStatement(parser.getClause());
            int parameterIndex = 1;
            for (String parameter : parser.getParameters())
                preparedStatement.setObject(parameterIndex++, query.parameters().get(parameter));
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

        public Query clause() {
            return query;
        }

        public T select(String select) {
            return createInstance(query.select(select));
        }

        public T where(String clause) {
            return createInstance(query.where(clause));
        }

        public T where(String clause, Map<String, Object> parameters) {
            return createInstance(query.where(clause).parameters(parameters));
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
            return builder.rowMethod(table.name(), column)
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

        public LinkedRow belongsTo(String parentTable) {
            return join(parentTable).asParent().exactlyOne();
        }

        public LinkedTable join(String anotherTable) {
            return new LinkedTable(this, anotherTable);
        }

        public LinkedRow hasOne(String childTableOrManyToMany) {
            return join(childTableOrManyToMany).asChildren().exactlyOne();
        }

        public LinkedTable hasMany(String childTableOrManyToMany) {
            return join(childTableOrManyToMany).asChildren();
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
            query.parameters().putAll(row.table().clause().parameters());
            query.parameters().putAll(row.data());
        }

        public LinkedTable(Row<? extends Table<?>> row, String name, Query query) {
            super(name, query);
            this.row = row;
        }

        public LinkedTable defaultParameterColumn(String parameterColumn) {
            return createInstance(query.defaultParameterColumn(parameterColumn));
        }

        public LinkedTable defaultLinkColumn(String linkColumn) {
            return createInstance(query.defaultLinkColumn(linkColumn));
        }

        public LinkedTable on(String condition) {
            return createInstance(query.on(condition));
        }

        @Override
        protected LinkedTable createInstance(Query query) {
            return new LinkedTable(row, name(), query);
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
            return defaultLinkColumn(builder.referencedColumnStrategy(this, row.table()))
                    .defaultParameterColumn(builder.joinColumnStrategy(this, row.table()));
        }

        public LinkedTable asChildren() {
            return defaultLinkColumn(builder.joinColumnStrategy(row.table(), this))
                    .defaultParameterColumn(builder.referencedColumnStrategy(row.table(), this));
        }

        public LinkedTable through(String table) {
            LinkedTable throughTable = row.join(table).asChildren();
            String joinColumn = builder.joinColumnStrategy(this, throughTable);
            return new LinkedThroughTable(this, throughTable.select(joinColumn));
        }

        public LinkedThroughTable through(String table, String joinColumn) {
            return new LinkedThroughTable(this, row.join(table).asChildren().select(joinColumn));
        }
    }

    public class LinkedThroughTable extends LinkedTable {
        private final LinkedTable thoughTable;
        private final String referencedColumn;

        public LinkedThroughTable(LinkedTable linkedTable, LinkedTable thoughTable) {
            super(linkedTable.row, linkedTable.name(), linkedTable.clause().on(null));
            this.thoughTable = thoughTable;
            referencedColumn = linkedTable.clause().linkColumn() == null ? "" +
                    builder.referencedColumnStrategy(linkedTable, thoughTable)
                    : linkedTable.clause().linkColumn();
        }

        public LinkedThroughTable(LinkedTable thoughTable, String referencedColumn,
                                  Row<? extends Table<?>> row, String name, Query query) {
            super(row, name, query);
            this.thoughTable = thoughTable;
            this.referencedColumn = referencedColumn;
        }

        @Override
        protected String buildSql() {
            return clause().where(String.format("%s in (%s)", referencedColumn, thoughTable.buildSql())).buildSql();
        }

        @Override
        protected LinkedTable createInstance(Query query) {
            return new LinkedThroughTable(thoughTable, referencedColumn, row, name(), query);
        }

        @Override
        public LinkedTable on(String condition) {
            return new LinkedThroughTable(thoughTable.on(condition), referencedColumn, row, name(), query);
        }
    }

    public class LinkedRow extends Row<LinkedTable> {
        protected Supplier<Map<String, Object>> query;

        public LinkedRow(LinkedTable table, Supplier<Map<String, Object>> query) {
            super(table, null);
            this.query = query;
        }

        @Override
        public Map<String, Object> data() {
            if (data == null)
                data = query.get();
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
