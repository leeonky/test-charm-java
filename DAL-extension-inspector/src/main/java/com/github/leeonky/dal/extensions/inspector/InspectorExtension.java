package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

public class InspectorExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        Inspector inspector = Inspector.inspector();
        inspector.launch();

        dal.getRuntimeContextBuilder()
                .registerErrorHook((input, code, error) -> inspector.inspectViaMode(dal, input, code))
                .registerMetaProperty("inspect", metaData -> {
                    Object input = metaData.data().instance();
                    inspector.inspect(dal, input, "{}");
                    return input;
                });
    }
}
