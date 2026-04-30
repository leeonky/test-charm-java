package org.testcharm.dal.extensions.basic.zip.util;

import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.inspector.Dumper;
import org.testcharm.dal.runtime.inspector.DumpingBuffer;

public class ZipBinaryDumper implements Dumper<ZipBinary> {

    @Override
    public void dump(Data<ZipBinary> data, DumpingBuffer context) {
        DumpingBuffer sub = context.append("zip archive").fork();
        data.list().wraps().values().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Data<ZipBinary> data, DumpingBuffer context) {
        DumpingBuffer sub = context.fork();
        data.list().wraps().values().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }
}
