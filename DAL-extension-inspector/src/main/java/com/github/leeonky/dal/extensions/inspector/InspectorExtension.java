package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

public class InspectorExtension implements Extension {

    private static Inspector inspector;

    public static void launch() {
        if (inspector == null) {
            inspector = new Inspector();
        }
    }

    public static void shutdown() {
        if (inspector != null) {
            inspector.exit();
            inspector = null;
        }
    }

    @Override
    public void extend(DAL dal) {
        launch();
        dal.getRuntimeContextBuilder()
                .registerErrorHook((input, code, error) -> inspector.inspectViaMode(dal, input, code))
                .registerMetaProperty("inspect", metaData -> {
                    Object input = metaData.data().instance();
                    inspector.inspect(dal, input, "{}");
                    return input;
                });
    }
}
