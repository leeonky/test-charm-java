package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.CollectionDALCollection;
import com.github.leeonky.dal.runtime.DALCollection;
import com.github.leeonky.dal.runtime.DALCollectionFactory;

import java.io.File;

public class FileDALCollectionFactory implements DALCollectionFactory<File, File> {
    @Override
    public boolean isList(File file) {
        return file.isDirectory();
    }

    @Override
    public DALCollection<File> create(File file) {
        return new CollectionDALCollection<>(Util.listFile(file));
    }
}
