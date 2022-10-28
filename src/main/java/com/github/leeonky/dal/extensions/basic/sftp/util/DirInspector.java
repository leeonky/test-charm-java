package com.github.leeonky.dal.extensions.basic.sftp.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorContext;
import com.github.leeonky.dal.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class DirInspector implements Inspector {
    @Override
    public String inspect(Data data, InspectorContext context) {
        SFtpFile sFtpFile = (SFtpFile) data.getInstance();
        return String.join("\n", new ArrayList<String>() {{
            add(sFtpFile.remoteInfo());
            data.getDataList().stream().map(Data::dump).forEach(this::add);
        }});
    }

    @Override
    public String dump(Data data, InspectorContext context) {
        String name = ((SFtpFile) data.getInstance()).name() + "/";
        List<Data> dataList = data.getDataList();
        if (dataList.isEmpty())
            return name;
        return name + "\n" + dataList.stream().map(Data::dump).map(TextUtil::indent).collect(Collectors.joining("\n"));
    }
}
