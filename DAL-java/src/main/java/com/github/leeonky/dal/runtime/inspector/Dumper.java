package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data.Resolved;

public interface Dumper {
    Dumper STRING_DUMPER = new StringDumper(),
            VALUE_DUMPER = new ValueDumper(),
            LIST_DUMPER = new ListDumper(),
            MAP_DUMPER = new MapDumper();

    void dump(Resolved data, DumpingBuffer dumpingBuffer);

    default void dumpValue(Resolved data, DumpingBuffer dumpingBuffer) {
        dump(data, dumpingBuffer);
    }

    interface Cacheable extends Dumper {

        @Override
        default void dump(Resolved data, DumpingBuffer context) {
            context.cached(data, () -> cachedInspect(data, context));
        }

        @Override
        default void dumpValue(Resolved data, DumpingBuffer context) {
            context.cached(data, () -> cachedDump(data, context));
        }

        default void cachedDump(Resolved data, DumpingBuffer context) {
            cachedInspect(data, context);
        }

        void cachedInspect(Resolved data, DumpingBuffer context);
    }
}
