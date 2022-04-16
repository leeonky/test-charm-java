package com.github.leeonky.dal.extensions;

import com.github.leeonky.util.Suppressor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.github.leeonky.dal.extensions.BinaryExtension.readAll;
import static java.util.Arrays.stream;

//TODO merge zip tree and node zip node group and zip tree group
public class ZipFileTree implements Iterable<ZipFileTree.ZipNode> {
    private final byte[] data;
    private final Map<String, ZipNode> children;

    public ZipFileTree(byte[] data) {
        this.data = data;
        children = Suppressor.get(() -> new LinkedHashMap<String, ZipNode>() {{
            unzipToMemory().forEach((entry, bytes) -> {
                LinkedList<String> nameList = stream(entry.getName().split("/")).filter(s -> !s.isEmpty())
                        .collect(Collectors.toCollection(LinkedList::new));
                addNode(nameList, entry, this, bytes);
            });
        }});
    }

    private TreeMap<ZipEntry, byte[]> unzipToMemory() throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(data))) {
            TreeMap<ZipEntry, byte[]> treeMap = new TreeMap<>(Comparator.comparing(ZipEntry::getName));
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                treeMap.put(zipEntry, readAll(zipInputStream));
                zipEntry = zipInputStream.getNextEntry();
            }
            return treeMap;
        }
    }

    private void addNode(LinkedList<String> path, ZipEntry zipEntry, Map<String, ZipNode> children, byte[] bytes) {
        String name = path.pop();
        if (path.size() == 0)
            children.put(name, new ZipNode(zipEntry, name, bytes));
        else
            addNode(path, zipEntry, children.get(name).children, bytes);
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

    public static class ZipNode implements Iterable<ZipNode> {
        private final String name;
        private final Map<String, ZipNode> children = new LinkedHashMap<>();
        private final boolean directory;
        private final byte[] bytes;

        public ZipNode(ZipEntry entry, String name, byte[] bytes) {
            this.name = name;
            directory = entry.isDirectory();
            this.bytes = bytes;
        }

        @Override
        public String toString() {
            return name;
        }

        public String name() {
            return name;
        }

        public InputStream open() {
            return new ByteArrayInputStream(bytes);
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
            return directory;
        }
    }
}
