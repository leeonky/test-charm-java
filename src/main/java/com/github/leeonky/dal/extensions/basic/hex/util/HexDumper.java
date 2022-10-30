package com.github.leeonky.dal.extensions.basic.hex.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.runtime.inspector.DumpingContext;

public class HexDumper implements Dumper {

    @Override
    public void dumpDetail(Data data, DumpingContext context) {
        context.append(data.getInstance().toString());
    }
}
