package com.github.leeonky.dal.extensions.basic.zip.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingContext;

public class ZipBinaryDumper implements Dumper {

    @Override
    public void dump(Data data, DumpingContext context) {
        DumpingContext sub = context.append("zip archive").sub();
        data.getDataList().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }

    @Override
    public void dumpValue(Data data, DumpingContext context) {
        DumpingContext sub = context.sub();
        data.getDataList().forEach(subFile -> sub.newLine().dumpValue(subFile));
    }
}
