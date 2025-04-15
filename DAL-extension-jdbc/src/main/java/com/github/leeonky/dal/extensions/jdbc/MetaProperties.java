package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.runtime.Callable;
import com.github.leeonky.dal.runtime.RuntimeData;

public class MetaProperties {
    public static Callable<String, ?> hasOne(RuntimeData<DataBase.Row> metaData) {
        return metaData.data().instance()::hasOne;
    }

    public static Callable<String, DataBase.LinkedTable> hasMany(RuntimeData<DataBase.Row> metaData) {
        return metaData.data().instance()::hasMany;
    }

    public static Callable<String, ?> on(RuntimeData<Association> metaData) {
        return metaData.data().instance()::on;
    }

    public static Callable<String, ?> through(RuntimeData<Association> metaData) {
        return metaData.data().instance()::throughWithColumn;
    }

    public static Callable<String, ?> where(RuntimeData<CanWhere> metaData) {
        return metaData.data().instance()::where;
    }

    public static Callable<String, ?> select(RuntimeData<DataBase.Table> metaData) {
        return metaData.data().instance()::select;
    }

    public static Callable<String, ?> belongsTo(RuntimeData<DataBase.Row> metaData) {
        return metaData.data().instance()::belongsTo;
    }
}
