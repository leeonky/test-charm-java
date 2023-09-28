package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.jdbc.DataBase.Table.Row.BelongsTo;
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
                .registerMetaProperty("belongsTo", MetaProperties::belongsTo)
                .registerMetaProperty("on", MetaProperties::on)
                .registerMetaProperty("where", MetaProperties::on)
                .registerPropertyAccessor(Callable.class, new JavaClassPropertyAccessor<Callable>(BeanClass.create(Callable.class)) {
                    @Override
                    public Set<Object> getPropertyNames(Callable callable) {
                        return Collections.emptySet();
                    }

                    @Override
                    public Object getValue(Callable callable, Object property) {
                        return callable.apply(property);
                    }
                })
                .registerPropertyAccessor(BelongsTo.class, new JavaClassPropertyAccessor<BelongsTo>(BeanClass.create(BelongsTo.class)) {
                    @Override
                    public Object getValue(BelongsTo belongsTo, Object property) {
// TODO                        need test
                        belongsTo.query();
                        return belongsTo.getValue(String.valueOf(property));
                    }

                    @Override
                    public Set<Object> getPropertyNames(BelongsTo belongsTo) {
// TODO                        need test
                        belongsTo.query();
                        return new LinkedHashSet<>(belongsTo.keys());
                    }
                })
        ;
    }
}

