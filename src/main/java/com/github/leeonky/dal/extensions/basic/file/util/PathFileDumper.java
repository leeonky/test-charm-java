package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.nio.file.Path;

public class PathFileDumper implements Dumper {

    @Override
    public void dump(Data path, DumpingBuffer context) {
        context.append("java.nio.Path").newLine().dumpValue(path);
    }

    @Override
    public void dumpValue(Data data, DumpingBuffer context) {
        context.append(Util.attribute((Path) data.getInstance()));
    }
}
