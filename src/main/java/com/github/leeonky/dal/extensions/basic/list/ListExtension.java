package com.github.leeonky.dal.extensions.basic.list;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Callable;
import com.github.leeonky.dal.runtime.DALCollection;
import com.github.leeonky.dal.runtime.Extension;

public class ListExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerMetaProperty("top", metaData -> (Callable<Integer, DALCollection<Object>>) size ->
                metaData.data().list(metaData.inputNode().getOperandPosition()).limit(size));
    }
}
