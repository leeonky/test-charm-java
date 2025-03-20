package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data.Resolved;
import com.github.leeonky.util.Classes;

import java.util.Map;
import java.util.Set;

public class MapDumper implements Dumper.Cacheable {

    @Override
    public void cachedInspect(Resolved data, DumpingBuffer context) {
        dumpType(data, context);
        dumpBody(data, context);
    }

    private void dumpBody(Resolved data, DumpingBuffer dumpingBuffer) {
        DumpingBuffer indentContext = dumpingBuffer.append("{").indent();
        getFieldNames(data).forEach(fieldName -> {
            dumpField(data, fieldName, indentContext.sub(fieldName).newLine());
            indentContext.appendThen(",");
        });
        dumpingBuffer.optionalNewLine().append("}");
    }

    protected void dumpField(Resolved data, Object field, DumpingBuffer context) {
        context.append(key(field)).append(": ");
        context.dumpValue(data.getValueData(field));
    }

    protected String key(Object o) {
        return String.valueOf(o);
    }

    protected Set<?> getFieldNames(Resolved data) {
        return data.fieldNames();
    }

    protected void dumpType(Resolved data, DumpingBuffer dumpingBuffer) {
        if (!(data.instanceOf(Map.class)))
            dumpingBuffer.append(Classes.getClassName(data.value())).appendThen(" ");
    }
}
