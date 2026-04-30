package org.testcharm.dal.runtime.inspector;

import org.testcharm.dal.runtime.Data;
import org.testcharm.util.Classes;

public class ValueDumper<T> implements Dumper<T> {
    protected void inspectType(Data<T> data, DumpingBuffer dumpingBuffer) {
        dumpingBuffer.append(Classes.getClassName(data.value()));
    }

    protected void inspectValue(Data<T> data, DumpingBuffer dumpingBuffer) {
        dumpingBuffer.append("<" + data.value() + ">");
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
        dumpingBuffer.defer(" ");
        inspectValue(data, dumpingBuffer);
    }
}
