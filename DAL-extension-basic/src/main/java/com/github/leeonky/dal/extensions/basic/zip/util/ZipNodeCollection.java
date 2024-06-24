package com.github.leeonky.dal.extensions.basic.zip.util;

import com.github.leeonky.util.InvocationException;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.zip.ZipEntry;

public abstract class ZipNodeCollection implements Iterable<ZipBinary.ZipNode> {
    protected final Map<String, ZipBinary.ZipNode> children = new LinkedHashMap<>();

    public Set<String> list() {
        return children.keySet();
    }

    @Override
    public Iterator<ZipBinary.ZipNode> iterator() {
        return children.values().iterator();
    }

    public ZipBinary.ZipNode createSub(String fileName) {
        ZipBinary.ZipNode zipNode = children.get(fileName);
        if (zipNode == null)
            throw new InvocationException(new FileNotFoundException(String.format("File `%s` not exist", fileName)));
        return zipNode;
    }

    public Object getSub(String name) {
        ZipBinary.ZipNode zipNode = children.get(name);
        if (zipNode != null)
            return zipNode;
        if (list().stream().anyMatch(f -> f.startsWith(name + ".")))
            return new ZipFileFileGroup(this, name);
        throw new InvocationException(new FileNotFoundException(String.format("File or File Group <%s> not found", name)));
    }

    public void addNode(LinkedList<String> path, ZipEntry zipEntry, byte[] bytes) {
        String name = path.pop();
        if (path.size() == 0)
            children.put(name, new ZipBinary.ZipNode(zipEntry, name, bytes));
        else
            children.get(name).addNode(path, zipEntry, bytes);
    }
}
