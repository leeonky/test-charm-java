package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.MetaData;
import com.github.leeonky.dal.runtime.RuntimeException;

import java.util.function.BiFunction;

public class MetaProperties {
    public static Callable<?> hasOne(MetaData metaData) {
        return call(metaData, DataBase.Row.class, DataBase.Row::hasOne, "`hasOne` meta property only apply to Row");
    }

    public static Callable<DataBase.LinkedTable> hasMany(MetaData metaData) {
        return call(metaData, DataBase.Row.class, DataBase.Row::hasMany, "`hasMany` meta property only apply to Row");
    }

    public static Callable<?> on(MetaData metaData) {
        return call(metaData, Association.class, Association::on, "`on` meta property only apply to Association");
    }

    public static Callable<?> through(MetaData metaData) {
        return call(metaData, Association.class, Association::throughWithColumn,
                "`where` meta property only apply Table or Association");
    }

    public static Callable<?> where(MetaData metaData) {
        return call(metaData, CanWhere.class, CanWhere::where,
                "`where` meta property only apply Table or Association");
    }

    public static Callable<?> select(MetaData metaData) {
        return call(metaData, DataBase.Table.class, DataBase.Table::select,
                "`select` meta property only apply Table");
    }

    public static Callable<?> belongsTo(MetaData metaData) {
        return call(metaData, DataBase.Row.class, DataBase.Row::belongsTo,
                "`belongsTo` meta property only apply Row");
    }

    @SuppressWarnings("unchecked")
    private static <T, R> Callable<R> call(MetaData metaData, Class<T> type, BiFunction<T, String, R> action, String message) {
        Data data = metaData.evaluateInput();
        if (type.isInstance(data.getInstance()))
            return arg -> action.apply((T) data.getInstance(), arg);
        throw new RuntimeException(message, metaData.getSymbolNode().getPositionBegin());
    }
}
