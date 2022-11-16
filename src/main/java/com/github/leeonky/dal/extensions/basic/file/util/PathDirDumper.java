package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class PathDirDumper implements Dumper {

    @Override
    public void dump(Data data, DumpingBuffer context) {
        DumpingBuffer sub = context.append("java.nio.Path").appendThen(" ").append(data.getInstance() + "/").sub();
        data.getDataList().forEach(subPath -> sub.newLine().dumpValue(subPath)); //will dump in FileDirDumper or FileFileDumper
    }
}
