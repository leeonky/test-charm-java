package com.github.leeonky.dal.extensions.basic.hex.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.DumpingContext;
import com.github.leeonky.dal.runtime.inspector.InspectorBk;

public class HexInspectorBk implements InspectorBk {

    @Override
    public String inspect(Data data, DumpingContext context) {
        return "Binary " + data.getInstance().toString();
    }
}
