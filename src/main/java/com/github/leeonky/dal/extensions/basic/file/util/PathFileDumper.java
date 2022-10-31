package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingContext;

import java.nio.file.Path;

public class PathFileDumper implements Dumper {

    @Override
    public void dump(Data path, DumpingContext context) {
        context.append("java.nio.Path").newLine().dumpValue(path);
    }

    @Override
    public void dumpValue(Data data, DumpingContext context) {
        context.append(Util.attribute((Path) data.getInstance()));
    }
}
