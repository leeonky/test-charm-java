package com.github.leeonky.dal.extensions.basic.zip.util;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class ZipBinaryDumper implements Dumper {

    @Override
    public void dump(Resolved data, DumpingBuffer context) {
        DumpingBuffer sub = context.append("zip archive").sub();
        data.eachSubData(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Resolved data, DumpingBuffer context) {
        DumpingBuffer sub = context.sub();
        data.eachSubData(subFile -> sub.newLine().dumpValue(subFile));
    }
}
