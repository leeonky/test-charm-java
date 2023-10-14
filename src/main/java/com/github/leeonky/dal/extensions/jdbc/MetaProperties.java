package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.runtime.MetaData;

public class MetaProperties {
    public static Callable<String, ?> hasOne(MetaData metaData) {
        return ((DataBase.Row<?>) metaData.getData().getInstance())::hasOne;
    }

    public static Callable<String, DataBase.LinkedTable> hasMany(MetaData metaData) {
        return ((DataBase.Row<?>) metaData.getData().getInstance())::hasMany;
    }

    public static Callable<String, ?> on(MetaData metaData) {
        return ((Association<?>) metaData.getData().getInstance())::on;
    }

    public static Callable<String, ?> through(MetaData metaData) {
        return ((Association<?>) metaData.getData().getInstance())::throughWithColumn;
    }

    public static Callable<String, ?> where(MetaData metaData) {
        return ((CanWhere<?>) metaData.getData().getInstance())::where;
    }

    public static Callable<String, ?> select(MetaData metaData) {
        return ((DataBase.Table<?>) metaData.getData().getInstance())::select;
    }

    public static Callable<String, ?> belongsTo(MetaData metaData) {
        return ((DataBase.Row<?>) metaData.getData().getInstance())::belongsTo;
    }
}
