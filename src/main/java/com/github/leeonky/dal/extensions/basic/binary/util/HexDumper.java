package com.github.leeonky.dal.extensions.basic.binary.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

public class HexDumper implements Dumper {

    @Override
    public void dump(Data data, DumpingBuffer context) {
        context.append(new Hex(Hex.getBytes(data)).toString());
    }
}
