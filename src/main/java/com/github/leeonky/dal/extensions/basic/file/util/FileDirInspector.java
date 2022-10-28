package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorContext;
import com.github.leeonky.dal.util.TextUtil;

import java.io.File;
import java.util.ArrayList;

class FileDirInspector implements Inspector {

    @Override
    public String inspect(Data data, InspectorContext context) {
        return String.join("\n", new ArrayList<String>() {{
            add("java.io.File " + ((File) data.getInstance()).getPath() + "/");
//            TODO use context dump method, avoid new root context
            data.getDataList().stream().map(Data::dump).forEach(this::add);
        }});
    }

    @Override
    public String dump(Data data, InspectorContext context) {
        return String.join("\n", new ArrayList<String>() {{
            add(((File) data.getInstance()).getName() + "/");
            data.getDataList().stream().map(Data::dump).map(TextUtil::indent).forEach(this::add);
        }});
    }
}
