package com.github.leeonky.dal.extensions.basic.list;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Callable;
import com.github.leeonky.dal.runtime.DALCollection;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;

import static com.github.leeonky.dal.runtime.ExpressionException.opt2;

public class ListExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerMetaProperty("top", metaData -> {
            Data.DataList list = opt2(metaData.data()::list);
            return (Callable<Integer, DALCollection<Object>>) list::limit;
        });
    }
}
