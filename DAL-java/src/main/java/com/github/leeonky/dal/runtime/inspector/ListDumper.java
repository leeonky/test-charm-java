package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.util.Classes;

import java.util.stream.Stream;

public class ListDumper implements Dumper.Cacheable {

    @Override
    public void cachedInspect(Resolved data, DumpingBuffer context) {
        dumpType(data, context);
        dumpBody(data, context);
    }

    private void dumpBody(Resolved data, DumpingBuffer dumpingBuffer) {
        dumpingBuffer.append("[").indent(indentBuffer ->
                data.list().wraps().forEach(ie -> {
                    indentBuffer.index(ie.index()).newLine().dumpValue(ie.value());
                    indentBuffer.appendThen(",");
                })).optionalNewLine().append("]");
    }

    protected void dumpType(Resolved data, DumpingBuffer context) {
        if (!(data.instanceOf(Iterable.class)) && !(data.instanceOf(Stream.class))
                && !data.value().getClass().isArray())
            context.append(Classes.getClassName(data.value())).appendThen(" ");
    }
}
