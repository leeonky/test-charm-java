package com.github.leeonky.dal.extensions;

import java.io.InputStream;
import java.util.stream.Stream;

public class ZipNodeFileGroup extends FileGroup<ZipFileTree.ZipNode> {
    private final ZipFileTree.ZipNode zipNode;

    public ZipNodeFileGroup(ZipFileTree.ZipNode zipNode, String name) {
        super(name);
        this.zipNode = zipNode;
    }

    @Override
    protected InputStream open(ZipFileTree.ZipNode subFile) {
        return subFile.open();
    }

    @Override
    protected ZipFileTree.ZipNode createSubFile(String fileName) {
        ZipFileTree.ZipNode zipNode = this.zipNode.findSub(fileName);
        if (zipNode == null)
            throw new IllegalArgumentException(String.format("File `%s` not exist", fileName));
        return zipNode;
    }

    @Override
    protected Stream<String> listFileName() {
        return zipNode.list().stream();
    }
}
