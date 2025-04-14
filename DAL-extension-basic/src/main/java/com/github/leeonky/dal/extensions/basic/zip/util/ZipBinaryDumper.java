package com.github.leeonky.dal.extensions.basic.zip.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class ZipBinaryDumper implements Dumper<ZipBinary> {

    @Override
    public void dump(Data<ZipBinary> data, DumpingBuffer context) {
        DumpingBuffer sub = context.append("zip archive").sub();
        data.list().wraps().values().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Data<ZipBinary> data, DumpingBuffer context) {
        DumpingBuffer sub = context.sub();
        data.list().wraps().values().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }
}
