package org.testcharm.dal.extensions.basic.file.util;

import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.inspector.Dumper;
import org.testcharm.dal.runtime.inspector.DumpingBuffer;

import java.nio.file.Path;

public class PathDirDumper implements Dumper<Path> {

    @Override
    public void dump(Data<Path> data, DumpingBuffer buffer) {
        DumpingBuffer sub = buffer.append("java.nio.Path").defer(" ").append(data.value() + "/").fork();
        data.list().wraps().values().forEach(subPath -> sub.newLine().dumpValue(subPath));
    }

    @Override
    public void dumpValue(Data<Path> data, DumpingBuffer buffer) {
        DumpingBuffer sub = buffer.append(data.value().toFile().getName()).append("/").indent();
        data.list().wraps().values().forEach(subPath -> sub.newLine().dumpValue(subPath));
    }
}
