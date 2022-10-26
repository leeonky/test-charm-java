package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorContext;

import java.io.File;

class FileFileInspector implements Inspector {

    @Override
    public String inspect(Data data, InspectorContext context) {
        return Util.attribute(((File) data.getInstance()).toPath());
    }
}
