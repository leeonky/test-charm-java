package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.MetaData;
import com.github.leeonky.dal.runtime.RuntimeException;

public class MetaProperties {
    @Deprecated
    public static Object belongsToBk(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBaseBk.Table.Row)
            return ((DataBaseBk.Table.Row) data.getInstance()).callBelongsTo();
        throw new RuntimeException("`belongsTo` meta property only apply DataBase.Table.Row", metaData.getSymbolNode().getPositionBegin());
    }

    public static Callable<?> hasOne(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.Row)
            return ((DataBase.Row) data.getInstance())::hasOne;
        throw new RuntimeException("`hasOne` meta property only apply DataBase.Table.Row", metaData.getSymbolNode().getPositionBegin());
    }

    public static Callable<DataBase.LinkedTable> hasMany(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.Row)
            return ((DataBase.Row) data.getInstance())::hasMany;
        throw new RuntimeException("`hasMany` meta property only apply DataBase.Table.Row", metaData.getSymbolNode().getPositionBegin());
    }

    public static Callable<?> on(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.LinkedRow)
            return ((DataBase.LinkedRow) data.getInstance())::on;
        if (data.getInstance() instanceof DataBase.LinkedTable)
            return ((DataBase.LinkedTable) data.getInstance())::on;
        throw new RuntimeException("Invalid meta property", metaData.getSymbolNode().getPositionBegin());
    }

    public static Callable<?> through(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.LinkedTable)
            return ((DataBase.LinkedTable) data.getInstance())::through;
        throw new RuntimeException("Invalid meta property", metaData.getSymbolNode().getPositionBegin());
    }

    public static Callable<?> where(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.Table)
            return ((DataBase.Table) data.getInstance())::where;
        if (data.getInstance() instanceof DataBase.LinkedRow)
            return ((DataBase.LinkedRow) data.getInstance())::where;
        throw new RuntimeException("Invalid meta property", metaData.getSymbolNode().getPositionBegin());
    }

    public static Callable<DataBase.Table> select(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.Table)
            return ((DataBase.Table) data.getInstance())::select;
        throw new RuntimeException("Invalid meta property", metaData.getSymbolNode().getPositionBegin());
    }

    public static Callable<?> belongsTo(MetaData metaData) {
        Data data = metaData.evaluateInput();
        if (data.getInstance() instanceof DataBase.Row)
            return ((DataBase.Row) data.getInstance())::belongsTo;
        throw new RuntimeException("`belongsTo` meta property only apply DataBase.Table.Row", metaData.getSymbolNode().getPositionBegin());
    }
}
