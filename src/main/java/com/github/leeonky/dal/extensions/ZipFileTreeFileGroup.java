package com.github.leeonky.dal.extensions;

import java.io.InputStream;
import java.util.stream.Stream;

public class ZipFileTreeFileGroup extends FileGroup<ZipFileTree.ZipNode> {
    private final ZipFileTree zipFileTree;

    public ZipFileTreeFileGroup(ZipFileTree zipFileTree, String name) {
        super(name);
        this.zipFileTree = zipFileTree;
    }

    @Override
    protected InputStream open(ZipFileTree.ZipNode subFile) {
        return subFile.open();
    }

    @Override
    protected ZipFileTree.ZipNode createSubFile(String fileName) {
        return zipFileTree.findSub(fileName).get();
    }

    @Override
    protected Stream<String> listFileName() {
        return zipFileTree.list();
    }

}
