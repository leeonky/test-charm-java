package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.jdbc.DataBase.Table.Row;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.github.leeonky.util.BeanClass.create;

public class JDBCExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("belongsTo", MetaProperties::belongsTo)
                .registerMetaProperty("hasMany", MetaProperties::hasMany)
                .registerMetaProperty("hasOne", MetaProperties::hasOne)
                .registerMetaProperty("on", MetaProperties::on)
                .registerMetaProperty("where", MetaProperties::on)
                .registerPropertyAccessor(DataBase.class, new JavaClassPropertyAccessor<DataBase>(create(DataBase.class)) {
                    @Override
                    public Set<Object> getPropertyNames(DataBase dataBase) {
                        return new LinkedHashSet<>(dataBase.allTableNames());
                    }

                    @Override
                    public Object getValue(DataBase dataBase, Object property) {
                        return dataBase.table((String) property);
                    }
                })
                .registerPropertyAccessor(Row.class, new JavaClassPropertyAccessor<Row>(create(Row.class)) {
                    @Override
                    public Set<Object> getPropertyNames(Row row) {
                        return new LinkedHashSet<>(row.columns());
                    }

                    @Override
                    public Object getValue(Row row, Object property) {
                        return row.get((String) property);
                    }

                    @Override
                    public boolean isNull(Row row) {
                        return !row.hasData();
                    }
                })
                .registerPropertyAccessor(Callable.class, new JavaClassPropertyAccessor<Callable>(create(Callable.class)) {
                    @Override
                    public Set<Object> getPropertyNames(Callable callable) {
                        return Collections.emptySet();
                    }

                    @Override
                    public Object getValue(Callable callable, Object property) {
                        return callable.apply(property);
                    }
                })
        ;
    }
}

