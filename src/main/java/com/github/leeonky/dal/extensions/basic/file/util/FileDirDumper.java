package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.io.File;

public class FileDirDumper implements Dumper {

    @Override
    public void dump(Data data, DumpingBuffer context) {
        DumpingBuffer sub = context.append("java.io.File").appendThen(" ")
                .append(((File) data.getInstance()).getPath()).append("/").sub();
        data.getDataList().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Data data, DumpingBuffer context) {
        DumpingBuffer sub = context.append(((File) data.getInstance()).getName()).append("/").indent();
        data.getDataList().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }
}
