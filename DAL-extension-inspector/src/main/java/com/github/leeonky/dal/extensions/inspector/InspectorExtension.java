package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;

public class InspectorExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        Inspector.launch();
        Inspector.register(dal);
        dal.getRuntimeContextBuilder()
                .registerErrorHook((input, code, error) -> Inspector.inspect(dal, input, code))
                .registerMetaProperty("inspect", metaData -> {
                    Data<?> data = metaData.data();
                    Inspector.inspect(dal, data, "{}");
                    return data.value();
                });
    }
}
