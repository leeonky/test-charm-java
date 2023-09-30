package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.jdbc.DataBase.Table.Row.BelongsTo;
import com.github.leeonky.dal.extensions.jdbc.DataBase.Table.Row.HasOne;
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
                .registerPropertyAccessor(BelongsTo.class, new JavaClassPropertyAccessor<BelongsTo>(create(BelongsTo.class)) {
                    @Override
                    public Object getValue(BelongsTo belongsTo, Object property) {
                        return belongsTo.getValue(String.valueOf(property));
                    }

                    @Override
                    public Set<Object> getPropertyNames(BelongsTo belongsTo) {
                        return new LinkedHashSet<>(belongsTo.keys());
                    }

                    @Override
                    public boolean isNull(BelongsTo belongsTo) {
                        return !belongsTo.hasData();
                    }
                })
                .registerPropertyAccessor(HasOne.class, new JavaClassPropertyAccessor<HasOne>(create(HasOne.class)) {
                    @Override
                    public Object getValue(HasOne hasOne, Object property) {
                        return hasOne.getValue(String.valueOf(property));
                    }

                    @Override
                    public Set<Object> getPropertyNames(HasOne hasOne) {
                        return new LinkedHashSet<>(hasOne.keys());
                    }

                    @Override
                    public boolean isNull(HasOne hasData) {
                        return !hasData.hasData();
                    }
                })
        ;
    }
}

