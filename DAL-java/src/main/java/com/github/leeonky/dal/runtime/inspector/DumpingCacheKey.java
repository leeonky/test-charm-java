package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data.Resolved;

import java.util.Objects;

class DumpingCacheKey {
    private final Resolved data;

    public DumpingCacheKey(Resolved data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        return Objects.hash((Object) data.value());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DumpingCacheKey && ((DumpingCacheKey) obj).data.value() == data.value();
    }
}
