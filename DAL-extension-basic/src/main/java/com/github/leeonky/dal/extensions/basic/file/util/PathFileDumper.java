package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

import java.nio.file.Path;

public class PathFileDumper implements Dumper<Path> {

    @Override
    public void dump(Data<Path> path, DumpingBuffer buffer) {
        buffer.append("java.nio.Path").newLine().dumpValue(path);
    }

    @Override
    public void dumpValue(Data<Path> data, DumpingBuffer buffer) {
        buffer.append(Util.attribute(data.value()));
    }
}
