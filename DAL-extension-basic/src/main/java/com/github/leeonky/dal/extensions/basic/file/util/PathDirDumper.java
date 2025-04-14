package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.nio.file.Path;

public class PathDirDumper implements Dumper {

    @Override
    public void dump(Data data, DumpingBuffer buffer) {
        DumpingBuffer sub = buffer.append("java.nio.Path").appendThen(" ").append(data.instance() + "/").sub();
        data.list().wraps().values().forEach(subPath -> sub.newLine().dumpValue(subPath));
    }

    @Override
    public void dumpValue(Data data, DumpingBuffer buffer) {
        DumpingBuffer sub = buffer.append(((Path) data.instance()).toFile().getName()).append("/").indent();
        data.list().wraps().values().forEach(subPath -> sub.newLine().dumpValue(subPath));
    }
}
