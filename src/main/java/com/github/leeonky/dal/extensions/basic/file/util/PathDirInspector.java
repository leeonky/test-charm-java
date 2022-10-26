package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorContext;
import com.github.leeonky.dal.util.TextUtil;

import java.nio.file.Path;
import java.util.ArrayList;

class PathDirInspector implements Inspector {
   
    @Override
    public String inspect(Data data, InspectorContext context) {
        return String.join("\n", new ArrayList<String>() {{
            add("java.nio.Path dir " + data.getInstance() + "/");
            data.getDataList().stream().map(Data::dump).forEach(this::add);
        }});
    }

    @Override
    public String dump(Data data, InspectorContext context) {
        return String.join("\n", new ArrayList<String>() {{
            add(((Path) data.getInstance()).getFileName() + "/");
            data.getDataList().stream().map(Data::dump).map(TextUtil::indent).forEach(this::add);
        }});
    }
}
