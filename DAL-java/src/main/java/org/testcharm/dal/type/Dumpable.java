package org.testcharm.dal.type;

import org.testcharm.dal.runtime.inspector.DumpingBuffer;

public interface Dumpable {
    void dump(DumpingBuffer dumpingBuffer);

    default void dumpValue(DumpingBuffer dumpingBuffer) {
        dump(dumpingBuffer);
    }
}
