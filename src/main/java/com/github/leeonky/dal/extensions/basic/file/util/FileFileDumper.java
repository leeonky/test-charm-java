package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.io.File;

public class FileFileDumper implements Dumper {

    @Override
    public void dump(Data data, DumpingBuffer buffer) {
        buffer.append("java.io.File").newLine().dumpValue(data);
    }

    @Override
    public void dumpValue(Data data, DumpingBuffer buffer) {
        buffer.append(Util.attribute(((File) data.instance()).toPath()));
    }
}
