package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.runtime.Callable;
import com.github.leeonky.dal.runtime.RuntimeData;

public class MetaProperties {
    public static Callable<String, ?> hasOne(RuntimeData<DataBase.Row> metaData) {
        return metaData.data().value()::hasOne;
    }

    public static Callable<String, DataBase.LinkedTable> hasMany(RuntimeData<DataBase.Row> metaData) {
        return metaData.data().value()::hasMany;
    }

    public static Callable<String, ?> on(RuntimeData<Association> metaData) {
        return metaData.data().value()::on;
    }

    public static Callable<String, ?> through(RuntimeData<Association> metaData) {
        return metaData.data().value()::throughWithColumn;
    }

    public static Callable<String, ?> where(RuntimeData<CanWhere> metaData) {
        return metaData.data().value()::where;
    }

    public static Callable<String, ?> select(RuntimeData<DataBase.Table> metaData) {
        return metaData.data().value()::select;
    }

    public static Callable<String, ?> belongsTo(RuntimeData<DataBase.Row> metaData) {
        return metaData.data().value()::belongsTo;
    }
}
