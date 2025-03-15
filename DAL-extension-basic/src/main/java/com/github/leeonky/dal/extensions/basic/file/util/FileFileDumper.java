package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.io.File;

public class FileFileDumper implements Dumper {

    @Override
    public void dump(Resolved data, DumpingBuffer buffer) {
        buffer.append("java.io.File").newLine().dumpValue(data.repack());
    }

    @Override
    public void dumpValue(Resolved data, DumpingBuffer buffer) {
        buffer.append(Util.attribute(((File) data.value()).toPath()));
    }
}
