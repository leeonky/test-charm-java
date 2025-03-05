package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

public class InspectorToolExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("watch", metaData -> {
                    Object instance = metaData.data().instance();
                    Inspector.watch(dal, metaData.runtimeContext(), metaData.inputNode().inspect(), instance);
                    return instance;
                });
    }
}
