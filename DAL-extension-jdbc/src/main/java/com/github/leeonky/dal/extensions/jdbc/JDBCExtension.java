package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.dal.runtime.inspector.KeyValueDumper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

public class JDBCExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty(CanWhere.class, "where", MetaProperties::where)
                .registerMetaProperty(DataBase.Table.class, "select", MetaProperties::select)
                .registerMetaProperty(DataBase.Row.class, "belongsTo", MetaProperties::belongsTo)
                .registerMetaProperty(Association.class, "on", MetaProperties::on)

                .registerMetaProperty(DataBase.Row.class, "hasMany", MetaProperties::hasMany)
                .registerMetaProperty(DataBase.Row.class, "hasOne", MetaProperties::hasOne)
                .registerMetaProperty(Association.class, "through", MetaProperties::through)

                .registerDumper(DataBase.Table.class, data -> new TableDumper())
                .registerDumper(DataBase.class, data -> new DataBaseDumper())
        ;
    }

    private static class TableDumper<T extends DataBase.Table<T>> implements Dumper<DataBase.Table<T>> {

        @Override
        public void dump(Data<DataBase.Table<T>> data, DumpingBuffer dumpingBuffer) {
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

        private List<List<String>> getData(Data<DataBase.Table<T>> data) {
            List<List<String>> tableData = new ArrayList<>();
            stream(data.value().spliterator(), false).limit(100).forEach(row -> {
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

    private static class DataBaseDumper extends KeyValueDumper<DataBase> {

        @Override
        protected void dumpType(Data<DataBase> data, DumpingBuffer dumpingBuffer) {
            dumpingBuffer.append("DataBase[").append(data.value().getUrl()).append("] ");
        }

        @Override
        protected void dumpField(Data<DataBase> data, Object field, DumpingBuffer context) {
            DataBase.Table<?> table = (DataBase.Table<?>) data.property(field).value();
            if (table.iterator().hasNext())
                context.append(key(field)).append(":").dumpValue(data.property(field));
        }
    }
}
