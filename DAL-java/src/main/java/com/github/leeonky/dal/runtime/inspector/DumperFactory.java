package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data;

import java.util.function.Function;

public interface DumperFactory<T> extends Function<Data<T>, Dumper<T>> {
    DumperFactory<Object> DUMPER_SKIP = skipTypeData -> (data, dumpingBuffer) -> dumpingBuffer.append("*skipped*");

    @SuppressWarnings("unchecked")
    static <T> DumperFactory<T> skip() {
        return (DumperFactory<T>) DUMPER_SKIP;
    }
}
