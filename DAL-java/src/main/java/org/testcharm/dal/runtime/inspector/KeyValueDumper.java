package org.testcharm.dal.runtime.inspector;

import org.testcharm.dal.runtime.Data;
import org.testcharm.util.Classes;

import java.util.Map;
import java.util.Set;

public class KeyValueDumper<T> implements Dumper.Cacheable<T> {

    @Override
    public void cachedInspect(Data<T> data, DumpingBuffer context) {
        dumpType(data, context);
        dumpBody(data, context);
    }

    private void dumpBody(Data<T> data, DumpingBuffer dumpingBuffer) {
        DumpingBuffer indent = dumpingBuffer.append("{").indent();
        getFieldNames(data).stream().sorted().forEach(fieldName -> {
            dumpField(data, fieldName, indent.sub(fieldName).newLine());
            indent.defer(",");
        });
        dumpingBuffer.optionalNewLine().append("}");
    }

    protected void dumpField(Data<T> data, Object field, DumpingBuffer context) {
        context.append(key(field)).append(": ");
        Data<?> value;
        try {
            value = data.property(field);
        } catch (Throwable e) {
            context.append(e);
            return;
        }
        context.dumpValue(value);
    }

    protected String key(Object o) {
        return String.valueOf(o);
    }

    protected Set<?> getFieldNames(Data<T> data) {
        return data.fieldNames();
    }

    protected void dumpType(Data<T> data, DumpingBuffer dumpingBuffer) {
        if (!(data.instanceOf(Map.class)))
            dumpingBuffer.append(Classes.getClassName(data.value())).defer(" ");
    }
}
