package com.github.leeonky.dal.extensions;

import com.github.leeonky.util.Suppressor;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.util.Arrays.stream;

//TODO merge zip tree and node zip node group and zip tree group
public class ZipFileTree implements Iterable<ZipFileTree.ZipNode> {
    private final ZipFile zipFile;
    private final Map<String, ZipNode> children = new LinkedHashMap<>();

    public ZipFileTree(ZipFile zipFile) {
        this.zipFile = zipFile;
        zipFile.stream().sorted(Comparator.comparing(ZipEntry::getName))
                .forEach(zipEntry -> addNode(stream(zipEntry.getName().split("/")).filter(s -> !s.isEmpty())
                        .collect(Collectors.toCollection(LinkedList::new)), zipEntry, children));

        return;
    }

    private void addNode(LinkedList<String> path, ZipEntry zipEntry, Map<String, ZipNode> children) {
        String name = path.pop();
        if (path.size() == 0)
            children.put(name, new ZipNode(zipEntry, name));
        else
            addNode(path, zipEntry, children.get(name).children);
    }

    @Override
    public Iterator<ZipNode> iterator() {
        return listNode().iterator();
    }

    public List<ZipNode> listNode() {
        return children.values().stream().sorted(Comparator.comparing(ZipNode::name)).collect(Collectors.toList());
    }

    public Object getSub(String name) {
        ZipNode zipNode = findSub(name);
        if (zipNode != null)
            return zipNode;
        if (list().stream().anyMatch(f -> f.startsWith(name + ".")))
            return new ZipFileTreeFileGroup(this, name);
        throw new IllegalArgumentException(String.format("File or File Group <%s> not found", name));
    }

    public ZipNode findSub(String name) {
        return children.get(name);
    }

    public Set<String> list() {
        return children.keySet();
    }

    public class ZipNode implements Iterable<ZipNode> {
        private final ZipEntry entry;
        private final String name;
        private final Map<String, ZipNode> children = new LinkedHashMap<>();

        public ZipNode(ZipEntry entry, String name) {
            this.entry = entry;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public String name() {
            return name;
        }

        public InputStream open() {
            return Suppressor.get(() -> zipFile.getInputStream(entry));
        }

        @Override
        public Iterator<ZipNode> iterator() {
            return listNode().iterator();
        }

        public List<ZipNode> listNode() {
            return children.values().stream().sorted(Comparator.comparing(ZipNode::name)).collect(Collectors.toList());
        }

        public Object getSub(String name) {
            ZipNode zipNode = findSub(name);
            if (zipNode != null)
                return zipNode;
            if (list().stream().anyMatch(f -> f.startsWith(name + ".")))
                return new ZipNodeFileGroup(this, name);
            throw new IllegalArgumentException(String.format("File or File Group <%s> not found", name));
        }

        public ZipNode findSub(String name) {
            return children.get(name);
        }

        public Set<String> list() {
            return children.keySet();
        }

        public boolean isDirectory() {
            return entry.isDirectory();
        }
    }
}
