package org.testcharm.dal.runtime.inspector;

import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.util.IndentBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.lang.String.format;
import static org.testcharm.dal.runtime.DALException.extractException;

public class DumpingBuffer {
    private final String path;
    private final IndentBuffer indentBuffer;
    private final DALRuntimeContext runtimeContext;
    private final AtomicInteger dumpedObjectCount;
    private final Map<DumpingCacheKey, String> caches;

    private DumpingBuffer(String path, IndentBuffer indentBuffer, DALRuntimeContext runtimeContext,
                          AtomicInteger dumpedObjectCount, Map<DumpingCacheKey, String> caches) {
        this.path = path;
        this.indentBuffer = indentBuffer;
        this.runtimeContext = runtimeContext;
        this.dumpedObjectCount = dumpedObjectCount;
        this.caches = caches;
    }

    public static DumpingBuffer rootContext(DALRuntimeContext context) {
        return new DumpingBuffer("root", IndentBuffer.create(context.maxDumpingLineCount()),
                context, new AtomicInteger(0), new HashMap<>());
    }

    public String getPath() {
        return path;
    }

    public DALRuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    private void checkCount() {
        if (dumpedObjectCount.getAndIncrement() == runtimeContext.maxDumpingObjectSize())
            throw new MaximizeDump();
    }

    public <T> DumpingBuffer dump(Data<T> data) {
        checkCount();
        try {
            runtimeContext.fetchDumper(data).dump(data, this);
        } catch (Throwable e) {
            append(e);
        }
        return this;
    }

    public <T> DumpingBuffer dumpValue(Data<T> data) {
        checkCount();
        try {
            runtimeContext.fetchDumper(data).dumpValue(data, this);
        } catch (Throwable e) {
            append(e);
        }
        return this;
    }

    public DumpingBuffer index(int index) {
        return new DumpingBuffer(format("%s[%d]", path, index), indentBuffer.fork(),
                runtimeContext, dumpedObjectCount, caches);
    }

    public DumpingBuffer sub(Object property) {
        return new DumpingBuffer(format("%s.%s", path, property), indentBuffer.fork(),
                runtimeContext, dumpedObjectCount, caches);
    }

    public DumpingBuffer indent() {
        return new DumpingBuffer(path, indentBuffer.indent(), runtimeContext, new AtomicInteger(0), caches);
    }

    public DumpingBuffer indent(Consumer<DumpingBuffer> subDump) {
        DumpingBuffer sub = indent();
        try {
            subDump.accept(sub);
        } catch (MaximizeDump ignore) {
            sub.newLine().append("*... Too many objects!*");
        }
        return this;
    }

    public DumpingBuffer fork() {
        return new DumpingBuffer(path, indentBuffer.fork(), runtimeContext, dumpedObjectCount, caches);
    }

    public void cached(Data<?> data, Runnable runnable) {
        DumpingCacheKey key = new DumpingCacheKey(data);
        String reference = caches.get(key);
        if (reference == null) {
            caches.put(key, path);
            runnable.run();
        } else {
            append("*reference* " + reference);
        }
    }

    public DumpingBuffer append(String s) {
        indentBuffer.append(s);
        return this;
    }

    public DumpingBuffer append(Throwable e) {
        return append("*throw* " + extractException(e).orElse(e));
    }

    public String content() {
        return indentBuffer.content();
    }

    public DumpingBuffer defer(String then) {
        indentBuffer.defer(then);
        return this;
    }

    public DumpingBuffer newLine() {
        indentBuffer.newLine();
        return this;
    }

    public DumpingBuffer optionalNewLine() {
        indentBuffer.optionalNewLine();
        return this;
    }

    public static class MaximizeDump extends RuntimeException {
    }
}
