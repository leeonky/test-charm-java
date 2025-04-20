package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data;

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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DumpingCacheKey)) return false;
        return ((DumpingCacheKey) obj).data.value() == data.value();
    }
}
