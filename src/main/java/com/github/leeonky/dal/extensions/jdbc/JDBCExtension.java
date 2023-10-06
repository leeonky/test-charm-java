package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.jdbc.DataBase.Row;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.dal.runtime.inspector.MapDumper;
import com.github.leeonky.util.BeanClass;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

public class JDBCExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("where", MetaProperties::where)
                .registerMetaProperty("select", MetaProperties::select)
                .registerMetaProperty("belongsTo", MetaProperties::belongsTo)
                .registerMetaProperty("on", MetaProperties::on)

                .registerMetaProperty("hasMany", MetaProperties::hasMany)
                .registerMetaProperty("hasOne", MetaProperties::hasOne)
                .registerMetaProperty("through", MetaProperties::through)

                .registerPropertyAccessor(DataBase.class, new DataBaseJavaClassPropertyAccessor())
                .registerPropertyAccessor(Row.class, new RowJavaClassPropertyAccessor())
                .registerPropertyAccessor(Callable.class, new CallableJavaClassPropertyAccessor())
                .registerDumper(DataBase.Table.class, data -> new TableDumper())
                .registerDumper(DataBase.class, data -> new DataBaseDumper())
        ;
    }

    private static class CallableJavaClassPropertyAccessor extends JavaClassPropertyAccessor<Callable> {
        public CallableJavaClassPropertyAccessor() {
            super(BeanClass.create(Callable.class));
        }

        @Override
        public Set<Object> getPropertyNames(Callable callable) {
            return Collections.emptySet();
        }

        @Override
        public Object getValue(Callable callable, Object property) {
            return callable.apply(property);
        }
    }

    private static class RowJavaClassPropertyAccessor extends JavaClassPropertyAccessor<Row> {
        public RowJavaClassPropertyAccessor() {
            super(BeanClass.create(Row.class));
        }

        @Override
        public Set<Object> getPropertyNames(Row row) {
            return new LinkedHashSet<>(row.columns());
        }

        @Override
        public Object getValue(Row row, Object property) {
            return row.column((String) property);
        }

        @Override
        public boolean isNull(Row row) {
            return row.empty();
        }
    }

    private static class DataBaseJavaClassPropertyAccessor extends JavaClassPropertyAccessor<DataBase> {
        public DataBaseJavaClassPropertyAccessor() {
            super(BeanClass.create(DataBase.class));
        }

        @Override
        public Set<Object> getPropertyNames(DataBase dataBase) {
            return new LinkedHashSet<>(dataBase.allTableNames());
        }

        @Override
        public Object getValue(DataBase dataBaseBk, Object property) {
            return dataBaseBk.table((String) property);
        }
    }

    private static class TableDumper implements Dumper {

        @Override
        public void dump(Data data, DumpingBuffer dumpingBuffer) {
            List<List<String>> tableData = getData(data);
            if (tableData.isEmpty())
                dumpingBuffer.append("[]");
            else {
                Integer[] lengths = resolveColumnWidth(tableData);
                tableData.forEach(line -> {
                    DumpingBuffer rowBuffer = dumpingBuffer.indent().newLine().append("|");
                    for (int c = 0; c < line.size(); c++)
                        rowBuffer.append(String.format(String.format(" %%%ds |", lengths[c]), line.get(c)));
                });
            }
        }

        private List<List<String>> getData(Data data) {
            List<List<String>> tableData = new ArrayList<>();
            stream(((DataBase.Table<?>) data.getInstance()).spliterator(), false).limit(100).forEach(row -> {
                if (tableData.isEmpty())
                    tableData.add(new ArrayList<>(row.columns()));
                tableData.add(row.data().values().stream().map(String::valueOf).collect(Collectors.toList()));
            });
            return tableData;
        }

        private Integer[] resolveColumnWidth(List<List<String>> tableData) {
            Integer[] lengths = tableData.get(0).stream().map(String::length).toArray(Integer[]::new);
            tableData.stream().skip(1).forEach(row -> {
                for (int c = 0; c < lengths.length; c++)
                    lengths[c] = Math.max(lengths[c], row.get(c).length());
            });
            return lengths;
        }
    }

    private static class DataBaseDumper extends MapDumper {

        @Override
        protected void dumpType(Data data, DumpingBuffer dumpingBuffer) {
            DataBase dataBase = (DataBase) data.getInstance();
            dumpingBuffer.append("DataBase[").append(dataBase.getUrl()).append("] ");
        }

        @Override
        protected void dumpField(Data data, Object field, DumpingBuffer context) {
            DataBase.Table<?> table = (DataBase.Table<?>) data.getValue(field).getInstance();
            if (table.iterator().hasNext())
                context.append(key(field)).append(":").dumpValue(data.getValue(field));
        }
    }
}
