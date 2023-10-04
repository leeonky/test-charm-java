package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.jdbc.DataBase.Row;
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
                .registerMetaProperty("where", MetaProperties::where)
                .registerMetaProperty("select", MetaProperties::select)
                .registerMetaProperty("belongsTo", MetaProperties::belongsTo)
                .registerMetaProperty("on", MetaProperties::on)

                .registerMetaProperty("hasMany", MetaProperties::hasMany)
                .registerMetaProperty("hasOne", MetaProperties::hasOne)
//                .registerMetaProperty("where", MetaProperties::on)
                .registerMetaProperty("through", MetaProperties::through)

                .registerPropertyAccessor(DataBaseBk.class, new JavaClassPropertyAccessor<DataBaseBk>(create(DataBaseBk.class)) {
                    @Override
                    public Set<Object> getPropertyNames(DataBaseBk dataBaseBk) {
                        return new LinkedHashSet<>(dataBaseBk.allTableNames());
                    }

                    @Override
                    public Object getValue(DataBaseBk dataBaseBk, Object property) {
                        return dataBaseBk.table((String) property);
                    }
                })
                .registerPropertyAccessor(DataBase.class, new JavaClassPropertyAccessor<DataBase>(create(DataBase.class)) {
                    @Override
                    public Set<Object> getPropertyNames(DataBase dataBase) {
                        return new LinkedHashSet<>(dataBase.allTableNames());
                    }

                    @Override
                    public Object getValue(DataBase dataBaseBk, Object property) {
                        return dataBaseBk.table((String) property);
                    }
                })
                .registerPropertyAccessor(Row.class, new JavaClassPropertyAccessor<Row>(create(Row.class)) {
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

