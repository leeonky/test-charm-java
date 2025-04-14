package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.io.File;

public class FileFileDumper implements Dumper<File> {

    @Override
    public void dump(Data<File> data, DumpingBuffer buffer) {
        buffer.append("java.io.File").newLine().dumpValue(data);
    }

    @Override
    public void dumpValue(Data<File> data, DumpingBuffer buffer) {
        buffer.append(Util.attribute(data.instance().toPath()));
    }
}
