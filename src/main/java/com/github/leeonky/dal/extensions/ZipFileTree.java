package com.github.leeonky.dal.extensions;

import com.github.leeonky.util.Suppressor;

import java.io.InputStream;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.lang.String.format;

public class ZipFileTree implements Iterable<ZipFileTree.ZipNode> {
    private final ZipFile zipFile;

    public ZipFileTree(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public Iterator<ZipNode> iterator() {
        return zipFile.stream().map(ZipNode::new).collect(Collectors.toList()).iterator();
    }

    public ZipNode getSub(String name) {
        return zipFile.stream().filter(zipEntry -> zipEntry.getName().equals(name)).findFirst().map(ZipNode::new)
                .orElseThrow(() -> new IllegalArgumentException(format("File <%s> not found in: `%s`", name, zipFile.getName())));
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

        public InputStream getBinary() {
            return Suppressor.get(() -> zipFile.getInputStream(entry));
        }
    }
}
