package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.util.Classes;

public class ValueDumper implements Dumper {
    protected void inspectType(Resolved data, DumpingBuffer dumpingBuffer) {
        dumpingBuffer.append(Classes.getClassName(data.value()));
    }

    protected void inspectValue(Resolved data, DumpingBuffer dumpingBuffer) {
        dumpingBuffer.append("<" + data.value() + ">");
    }

    @Override
    public void dump(Resolved data, DumpingBuffer dumpingBuffer) {
        inspectType(data, dumpingBuffer);
        dumpingBuffer.newLine();
        inspectValue(data, dumpingBuffer);
    }

    @Override
    public void dumpValue(Resolved data, DumpingBuffer dumpingBuffer) {
        inspectType(data, dumpingBuffer);
        dumpingBuffer.appendThen(" ");
        inspectValue(data, dumpingBuffer);
    }
}
