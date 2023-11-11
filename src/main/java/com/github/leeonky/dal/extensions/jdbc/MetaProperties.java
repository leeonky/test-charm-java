package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.runtime.Callable;
import com.github.leeonky.dal.runtime.RuntimeData;

public class MetaProperties {
    public static Callable<String, ?> hasOne(RuntimeData metaData) {
        return ((DataBase.Row<?>) metaData.data().instance())::hasOne;
    }

    public static Callable<String, DataBase.LinkedTable> hasMany(RuntimeData metaData) {
        return ((DataBase.Row<?>) metaData.data().instance())::hasMany;
    }

    public static Callable<String, ?> on(RuntimeData metaData) {
        return ((Association<?>) metaData.data().instance())::on;
    }

    public static Callable<String, ?> through(RuntimeData metaData) {
        return ((Association<?>) metaData.data().instance())::throughWithColumn;
    }

    public static Callable<String, ?> where(RuntimeData metaData) {
        return ((CanWhere<?>) metaData.data().instance())::where;
    }

    public static Callable<String, ?> select(RuntimeData metaData) {
        return ((DataBase.Table<?>) metaData.data().instance())::select;
    }

    public static Callable<String, ?> belongsTo(RuntimeData metaData) {
        return ((DataBase.Row<?>) metaData.data().instance())::belongsTo;
    }
}
