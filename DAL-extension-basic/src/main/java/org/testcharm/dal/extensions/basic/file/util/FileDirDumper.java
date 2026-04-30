package org.testcharm.dal.extensions.basic.file.util;

import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.inspector.Dumper;
import org.testcharm.dal.runtime.inspector.DumpingBuffer;

import java.io.File;

public class FileDirDumper implements Dumper<File> {

    @Override
    public void dump(Data<File> data, DumpingBuffer context) {
        DumpingBuffer sub = context.append("java.io.File").defer(" ")
                .append(data.value().getPath()).append("/").fork();
        data.list().wraps().values().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Data<File> data, DumpingBuffer context) {
        DumpingBuffer sub = context.append(data.value().getName()).append("/").indent();
        data.list().wraps().values().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }
}
