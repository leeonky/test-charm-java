package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

public class InspectorExtension implements Extension {
    @Override
    public void extend(DAL dal) {
        Inspector.launch();
        Inspector.register(dal);
        dal.getRuntimeContextBuilder()
                .registerErrorHook((input, code, error) -> Inspector.inspect(dal, input, code))
                .registerMetaProperty("inspect", metaData -> {
                    Object input = metaData.data().instance();
                    Inspector.inspect(dal, input, "{}");
                    return input;
                });
    }
}
