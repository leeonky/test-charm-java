package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;

public class InspectorToolExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("watch", metaData -> {
                    Data data = metaData.data();
                    Inspector.watch(dal, metaData.inputNode().inspect(), data);
                    return data.instance();
                });
    }
}
