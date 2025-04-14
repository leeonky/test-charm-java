package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;
import com.github.leeonky.dal.runtime.inspector.MapDumper;

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

    private static class TableDumper implements Dumper {

        @Override
        public void dump(Data<?> data, DumpingBuffer dumpingBuffer) {
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
            stream(((DataBase.Table<?>) data.instance()).spliterator(), false).limit(100).forEach(row -> {
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
        protected void dumpType(Data<?> data, DumpingBuffer dumpingBuffer) {
            DataBase dataBase = (DataBase) data.instance();
            dumpingBuffer.append("DataBase[").append(dataBase.getUrl()).append("] ");
        }

        @Override
        protected void dumpField(Data<?> data, Object field, DumpingBuffer context) {
            DataBase.Table<?> table = (DataBase.Table<?>) data.getValue(field).instance();
            if (table.iterator().hasNext())
                context.append(key(field)).append(":").dumpValue(data.getValue(field));
        }
    }
}
