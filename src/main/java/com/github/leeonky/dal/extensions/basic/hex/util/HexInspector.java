package com.github.leeonky.dal.extensions.basic.hex.util;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorContext;

public class HexInspector implements Inspector {
   
    @Override
    public String inspect(Data data, InspectorContext context) {
        return "Binary " + data.getInstance().toString();
    }
}
