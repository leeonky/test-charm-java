package com.github.leeonky.dal.extensions.basic.zip.util;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class ZipNodeDumper implements Dumper {

    @Override
    public void dump(Resolved data, DumpingBuffer context) {
        ZipBinary.ZipNode node = data.value();
        if (node.isDirectory()) {
            DumpingBuffer sub = context.append(node.name()).append("/").indent();
            data.eachSubData(subFile -> sub.newLine().dumpValue(subFile));
        } else if (node.name().toLowerCase().endsWith(".zip"))
            context.append(node.name()).indent().dumpValue(data.getValue("unzip"));
        else
            context.append(String.format("%s %6s %s", node.lastModifiedTime(), node.getSize(), node.name()));
    }
}
