package com.github.leeonky.dal.extensions;

import com.github.leeonky.util.Suppressor;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFileTree implements Iterable<ZipFileTree.ZipNode> {
    private final ZipFile zipFile;

    public ZipFileTree(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public Iterator<ZipNode> iterator() {
        return listNode().iterator();
    }

    public List<ZipNode> listNode() {
        return zipFile.stream().map(ZipNode::new).collect(Collectors.toList());
    }

    public Object getSub(String name) {
        Optional<ZipNode> zipNode = findSub(name);
        if (zipNode.isPresent())
            return zipNode.get();
        if (list().anyMatch(f -> f.startsWith(name + ".")))
            return new ZipFileTreeFileGroup(this, name);
        throw new IllegalArgumentException(String.format("File or File Group <%s> not found", name));
    }

    public Optional<ZipNode> findSub(String name) {
        return zipFile.stream().filter(zipEntry -> zipEntry.getName().equals(name)).findFirst().map(ZipNode::new);
    }

    public Stream<String> list() {
        return listNode().stream().map(ZipNode::name);
    }

    public class ZipNode {
        private final ZipEntry entry;

        public ZipNode(ZipEntry entry) {
            this.entry = entry;
        }

        @Override
        public String toString() {
            return entry.getName();
        }

        public String name() {
            return entry.getName();
        }

        public InputStream open() {
            return Suppressor.get(() -> zipFile.getInputStream(entry));
        }
    }
}
