package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorContext;

import java.nio.file.Path;

class PathFileInspector implements Inspector {
   
    @Override
    public String inspect(Data path, InspectorContext cache) {
        return Util.attribute((Path) path.getInstance());
    }
}
