package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data;

public interface Dumper<T> {
    Dumper<CharSequence> STRING_DUMPER = new StringDumper();
    Dumper<?> VALUE_DUMPER = new ValueDumper<>();
    Dumper<?> LIST_DUMPER = new ListDumper<>();
    Dumper<?> MAP_DUMPER = new KeyValueDumper<>();

    void dump(Data<T> data, DumpingBuffer dumpingBuffer);

    default void dumpValue(Data<T> data, DumpingBuffer dumpingBuffer) {
        dump(data, dumpingBuffer);
    }

    interface Cacheable<T> extends Dumper<T> {

        @Override
        default void dump(Data<T> data, DumpingBuffer context) {
            context.cached(data, () -> cachedInspect(data, context));
        }

        @Override
        default void dumpValue(Data<T> data, DumpingBuffer context) {
            context.cached(data, () -> cachedDump(data, context));
        }

        default void cachedDump(Data<T> data, DumpingBuffer context) {
            cachedInspect(data, context);
        }

        void cachedInspect(Data<T> data, DumpingBuffer context);
    }
}
