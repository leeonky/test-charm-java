package com.github.leeonky.dal.extensions;

import com.github.leeonky.util.Suppressor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.github.leeonky.dal.extensions.BinaryExtension.readAll;
import static java.util.Arrays.stream;

//TODO merge zip tree and node zip node group and zip tree group
public class ZipFileTree implements Iterable<ZipFileTree.ZipNode> {
    private final File file;
    private final Map<String, ZipNode> children;

    public ZipFileTree(File file) {
        this.file = file;
        children = fetchInZip(zipFile -> new LinkedHashMap<String, ZipNode>() {{
            zipFile.stream().sorted(Comparator.comparing(ZipEntry::getName)).forEach(zipEntry ->
                    addNode(stream(zipEntry.getName().split("/")).filter(s -> !s.isEmpty())
                            .collect(Collectors.toCollection(LinkedList::new)), zipEntry, this));
        }});
    }

    private <T> T fetchInZip(Function<ZipFile, T> action) {
        ZipFile zipFile = Suppressor.get(() -> new ZipFile(file));
        try {
            return action.apply(zipFile);
        } finally {
            Suppressor.run(zipFile::close);
        }
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
        private final String name;
        private final String fullName;
        private final Map<String, ZipNode> children = new LinkedHashMap<>();
        private final boolean directory;

        public ZipNode(ZipEntry entry, String name) {
            this.name = name;
            fullName = entry.getName();
            directory = entry.isDirectory();
        }

        @Override
        public String toString() {
            return name;
        }

        public String name() {
            return name;
        }

        public InputStream open() {
            return fetchInZip(zipFile -> zipFile.stream().filter(zipEntry -> zipEntry.getName().equals(fullName))
                    .map(entry -> Suppressor.get(() -> new ByteArrayInputStream(readAll(zipFile.getInputStream(entry)))))
                    .findFirst().orElse(null));
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
