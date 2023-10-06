package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.jdbc.DataBase.Row;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.util.BeanClass;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

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
}

