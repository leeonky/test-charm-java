package com.github.leeonky.dal.extensions;

import java.util.*;
import java.util.zip.ZipEntry;

public abstract class ZipNodeCollection implements Iterable<ZipFileTree.ZipNode> {
    protected final Map<String, ZipFileTree.ZipNode> children = new LinkedHashMap<>();

    public Set<String> list() {
        return children.keySet();
    }

    @Override
    public Iterator<ZipFileTree.ZipNode> iterator() {
        return children.values().iterator();
    }

    public ZipFileTree.ZipNode createSub(String fileName) {
        ZipFileTree.ZipNode zipNode = children.get(fileName);
        if (zipNode == null)
            throw new IllegalArgumentException(String.format("File `%s` not exist", fileName));
        return zipNode;
    }

    public Object getSub(String name) {
        ZipFileTree.ZipNode zipNode = children.get(name);
        if (zipNode != null)
            return zipNode;
        if (list().stream().anyMatch(f -> f.startsWith(name + ".")))
            return new ZipFileFileGroup(this, name);
        throw new IllegalArgumentException(String.format("File or File Group <%s> not found", name));
    }

    public void addNode(LinkedList<String> path, ZipEntry zipEntry, byte[] bytes) {
        String name = path.pop();
        if (path.size() == 0)
            children.put(name, new ZipFileTree.ZipNode(zipEntry, name, bytes));
        else
            children.get(name).addNode(path, zipEntry, bytes);
    }
}
