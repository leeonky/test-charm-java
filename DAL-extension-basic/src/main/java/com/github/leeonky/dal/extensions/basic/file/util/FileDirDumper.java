package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.io.File;

public class FileDirDumper implements Dumper {

    @Override
    public void dump(Resolved data, DumpingBuffer context) {
        DumpingBuffer sub = context.append("java.io.File").appendThen(" ")
                .append(((File) data.value()).getPath()).append("/").sub();
        data.eachSubData(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Resolved data, DumpingBuffer context) {
        DumpingBuffer sub = context.append(((File) data.value()).getName()).append("/").indent();
        data.eachSubData(subFile -> sub.newLine().dumpValue(subFile));
    }
}
