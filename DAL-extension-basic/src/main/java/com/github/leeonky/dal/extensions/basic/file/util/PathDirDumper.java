package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.nio.file.Path;

public class PathDirDumper implements Dumper {

    @Override
    public void dump(Resolved data, DumpingBuffer buffer) {
        DumpingBuffer sub = buffer.append("java.nio.Path").appendThen(" ").append(data.value() + "/").sub();
        data.eachSubData(subPath -> sub.newLine().dumpValue(subPath));
    }

    @Override
    public void dumpValue(Resolved data, DumpingBuffer buffer) {
        DumpingBuffer sub = buffer.append(((Path) data.value()).toFile().getName()).append("/").indent();
        data.eachSubData(subPath -> sub.newLine().dumpValue(subPath));
    }
}
