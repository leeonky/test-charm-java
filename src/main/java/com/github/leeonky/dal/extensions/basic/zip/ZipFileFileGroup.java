package com.github.leeonky.dal.extensions.basic.zip;

import com.github.leeonky.dal.extensions.basic.FileGroup;

import java.io.InputStream;
import java.util.stream.Stream;

public class ZipFileFileGroup extends FileGroup<ZipBinary.ZipNode> {
    private final ZipNodeCollection zipNodeCollection;

    public ZipFileFileGroup(ZipNodeCollection zipNode, String name) {
        super(name);
        zipNodeCollection = zipNode;
    }

    @Override
    protected InputStream open(ZipBinary.ZipNode subFile) {
        return subFile.open();
    }

    @Override
    protected ZipBinary.ZipNode createSubFile(String fileName) {
        return zipNodeCollection.createSub(fileName);
    }

    @Override
    protected Stream<String> listFileName() {
        return zipNodeCollection.list().stream();
    }
}
