package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.runtime.Flatten;
import com.github.leeonky.util.Suppressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileGroup implements Flatten, Iterable<File> {
    private final File folder;
    private final String name;
    private static final Map<String, Function<InputStream, Object>> fileExtensions = new HashMap<>();

    static {
        register("txt", file -> StringExtension.StaticMethods.string(BinaryExtension.readAll(file)));
        register("TXT", file -> StringExtension.StaticMethods.string(BinaryExtension.readAll(file)));
    }

    public static void register(String fileExtension, Function<InputStream, Object> fileReader) {
        fileExtensions.put(fileExtension, fileReader);
    }

    public FileGroup(File folder, String name) {
        this.folder = folder;
        this.name = name;
    }

    @Override
    public List<String> removeExpectedFields(Set<String> fields, Object symbol, Object property) {
        String fileName = fileName(property);
        if (fields.contains(fileName)) {
            fields.remove(fileName);
            return Collections.singletonList(fileName);
        }
        return Collections.emptyList();
    }

    private String fileName(Object fileExtension) {
        return String.format("%s.%s", name, fileExtension);
    }

    public Object getFile(String fileExtension) {
        String fileName = fileName(fileExtension);
        if (!new File(folder, fileName).exists())
            throw new IllegalArgumentException(String.format("File `%s` not exist", fileName));
        Function<InputStream, Object> handler = fileExtensions.get(fileExtension);
        if (handler != null)
            return handler.apply(Suppressor.get(() -> new FileInputStream(new File(folder, fileName))));
        return new File(folder, fileName);
    }

    public Set<String> listNames() {
        return listFileNames().map(s -> s.substring(name.length() + 1)).collect(Collectors.toSet());
    }

    private Stream<String> listFileNames() {
        return Arrays.stream(folder.list()).filter(n -> n.startsWith(name + "."));
    }

    @Override
    public Iterator<File> iterator() {
        return listFileNames().map(n -> new File(folder, n)).iterator();
    }
}
