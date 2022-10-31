package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.ListAccessor;

import java.nio.file.Path;

public class PathListAccessor implements ListAccessor<Path> {
    @Override
    public Iterable<?> toIterable(Path path) {
        return Util.listFile(path.toFile());
    }

    @Override
    public boolean isList(Path path) {
        return path.toFile().isDirectory();
    }
}
