package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.util.Classes;

public class ValueDumper<T> implements Dumper<T> {
    protected void inspectType(Data<T> data, DumpingBuffer dumpingBuffer) {
        dumpingBuffer.append(Classes.getClassName(data.instance()));
    }

    protected void inspectValue(Data<T> data, DumpingBuffer dumpingBuffer) {
        dumpingBuffer.append("<" + data.instance() + ">");
    }

    @Override
    public void dump(Data<T> data, DumpingBuffer dumpingBuffer) {
        inspectType(data, dumpingBuffer);
        dumpingBuffer.newLine();
        inspectValue(data, dumpingBuffer);
    }

    @Override
    public void dumpValue(Data<T> data, DumpingBuffer dumpingBuffer) {
        inspectType(data, dumpingBuffer);
        dumpingBuffer.appendThen(" ");
        inspectValue(data, dumpingBuffer);
    }
}
