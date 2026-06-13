package org.testcharm.dal.runtime.inspector;

import org.testcharm.dal.runtime.Data;
import org.testcharm.util.Classes;

import java.util.Objects;

class DumpingCacheKey {
    private final Data<?> data;

    public DumpingCacheKey(Data<?> data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data.value());
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return Classes.equals(this, obj, DumpingCacheKey.class, (self, another) ->
                self.data.value() == another.data.value());
    }
}
