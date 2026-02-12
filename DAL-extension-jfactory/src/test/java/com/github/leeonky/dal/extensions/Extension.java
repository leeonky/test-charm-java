package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.jfactory.Collector;

public class Extension implements com.github.leeonky.dal.runtime.Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty(Collector.class, "properties",
                        runtimeData -> runtimeData.data().value().properties())
                .registerMetaProperty(Collector.class, "build",
                        runtimeData -> runtimeData.data().value().build())
        ;
    }
}
