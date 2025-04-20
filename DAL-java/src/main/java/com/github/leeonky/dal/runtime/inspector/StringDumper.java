package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data;

public class StringDumper extends ValueDumper<CharSequence> {
    @Override
    protected void inspectValue(Data<CharSequence> data, DumpingBuffer dumpingBuffer) {
        dumpingBuffer.append("<").append(data.value().toString().replace("\\", "\\\\").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t").replace("\b", "\\b")).append(">");
    }
}
