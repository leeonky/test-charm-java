package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.ListAccessor;

import java.io.File;

public class FileListAccessor implements ListAccessor<File> {
    @Override
    public Iterable<?> toIterable(File file) {
        return Util.listFile(file);
    }

    @Override
    public boolean isList(File file) {
        return file.isDirectory();
    }
}
