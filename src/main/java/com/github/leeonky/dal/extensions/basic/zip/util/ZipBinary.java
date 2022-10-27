package com.github.leeonky.dal.extensions.basic.zip.util;

import com.github.leeonky.util.Suppressor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.github.leeonky.dal.extensions.basic.BinaryExtension.readAll;
import static java.util.Arrays.stream;

public class ZipBinary extends ZipNodeCollection {

    public ZipBinary(byte[] data) {
        Suppressor.run(() -> unzipToMemory(data).forEach((entry, bytes) ->
                addNode(stream(entry.getName().split("/")).filter(s -> !s.isEmpty())
                        .collect(Collectors.toCollection(LinkedList::new)), entry, bytes)));
    }

    private TreeMap<ZipEntry, byte[]> unzipToMemory(byte[] data) throws IOException {
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

    public static class ZipNode extends ZipNodeCollection {
        private final ZipEntry entry;
        private final String name;
        private final boolean directory;
        private final byte[] bytes;

        public ZipNode(ZipEntry entry, String name, byte[] bytes) {
            this.entry = entry;
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

        public boolean isDirectory() {
            return directory;
        }

        public long getSize() {
            return entry.getSize();
        }

        public Instant lastModifiedTime() {
            return entry.getLastModifiedTime().toInstant();
        }
    }
}
