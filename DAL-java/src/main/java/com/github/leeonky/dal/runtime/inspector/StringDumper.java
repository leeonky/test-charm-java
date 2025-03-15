package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data.Resolved;

public class StringDumper extends ValueDumper {
    @Override
    protected void inspectValue(Resolved data, DumpingBuffer dumpingBuffer) {
        dumpingBuffer.append("<").append(data.value().toString().replace("\\", "\\\\").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t").replace("\b", "\\b")).append(">");
    }
}
