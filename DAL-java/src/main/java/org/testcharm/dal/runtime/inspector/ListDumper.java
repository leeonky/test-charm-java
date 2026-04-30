package org.testcharm.dal.runtime.inspector;

import org.testcharm.dal.runtime.Data;
import org.testcharm.util.Classes;

import java.util.stream.Stream;

public class ListDumper<T> implements Dumper.Cacheable<T> {

    @Override
    public void cachedInspect(Data<T> data, DumpingBuffer context) {
        dumpType(data, context);
        dumpBody(data, context);
    }

    private void dumpBody(Data<T> data, DumpingBuffer dumpingBuffer) {
        dumpingBuffer.append("[").indent(indentBuffer ->
                data.list().wraps().forEach(ie -> {
                    indentBuffer.index(ie.index()).newLine().dumpValue(ie.value());
                    indentBuffer.defer(",");
                })).optionalNewLine().append("]");
    }

    protected void dumpType(Data<T> data, DumpingBuffer context) {
        if (!data.instanceOf(Iterable.class) && !data.instanceOf(Stream.class) && !data.value().getClass().isArray())
            context.append(Classes.getClassName(data.value())).defer(" ");
    }
}
