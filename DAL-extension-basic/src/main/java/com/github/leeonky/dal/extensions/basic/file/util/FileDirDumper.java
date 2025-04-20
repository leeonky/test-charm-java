package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.io.File;

public class FileDirDumper implements Dumper<File> {

    @Override
    public void dump(Data<File> data, DumpingBuffer context) {
        DumpingBuffer sub = context.append("java.io.File").appendThen(" ")
                .append(data.value().getPath()).append("/").sub();
        data.list().wraps().values().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Data<File> data, DumpingBuffer context) {
        DumpingBuffer sub = context.append(data.value().getName()).append("/").indent();
        data.list().wraps().values().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }
}
