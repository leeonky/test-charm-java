package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorContext;

import java.nio.file.Path;

class PathFileInspector implements Inspector {

    @Override
    public String inspect(Data path, InspectorContext context) {
        return "java.nio.Path\n" + dump(path, context);
    }

    @Override
    public String dump(Data data, InspectorContext context) {
        return Util.attribute((Path) data.getInstance());
    }
}
