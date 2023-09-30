package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.MetaData;
import com.github.leeonky.dal.runtime.RuntimeException;

public class MetaProperties {
    public static Object belongsTo(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.Table.Row)
            return ((DataBase.Table.Row) data.getInstance()).callBelongsTo();
        throw new RuntimeException("`belongsTo` meta property only apply DataBase.Table.Row", metaData.getSymbolNode().getPositionBegin());
    }

    public static Object hasOne(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.Table.Row)
            return ((DataBase.Table.Row) data.getInstance()).callHasOne();
        throw new RuntimeException("`hasOne` meta property only apply DataBase.Table.Row", metaData.getSymbolNode().getPositionBegin());
    }

    public static Object on(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.Table.Row.BelongsTo)
            return ((DataBase.Table.Row.BelongsTo) data.getInstance()).clause();
        else if (data.getInstance() instanceof DataBase.Table.Row.HasMany)
            return ((DataBase.Table.Row.HasMany) data.getInstance()).clause();
        else if (data.getInstance() instanceof DataBase.Table.Row.HasOne)
            return ((DataBase.Table.Row.HasOne) data.getInstance()).clause();
        throw new RuntimeException("Invalid meta property", metaData.getSymbolNode().getPositionBegin());
    }

    public static Object hasMany(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.Table.Row)
            return ((DataBase.Table.Row) data.getInstance()).callHasMany();
        throw new RuntimeException("`hasMany` meta property only apply DataBase.Table.Row", metaData.getSymbolNode().getPositionBegin());
    }
}
