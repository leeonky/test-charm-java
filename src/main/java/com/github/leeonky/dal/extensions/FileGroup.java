package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.runtime.Flatten;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class FileGroup<T> implements Flatten, Iterable<T> {
    protected static final Map<String, Function<InputStream, Object>> fileExtensions = new HashMap<>();
    protected final String name;

    static {
        register("txt", inputStream -> StringExtension.StaticMethods.string(BinaryExtension.readAll(inputStream)));
        register("TXT", inputStream -> StringExtension.StaticMethods.string(BinaryExtension.readAll(inputStream)));
    }

    public FileGroup(String name) {
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

    protected String fileName(Object fileExtension) {
        return String.format("%s.%s", name, fileExtension);
    }

    public static void register(String fileExtension, Function<InputStream, Object> fileReader) {
        fileExtensions.put(fileExtension, fileReader);
    }

    public Object getFile(String extensionName) {
        T subFile = createSubFile(fileName(extensionName));
        Function<InputStream, Object> handler = fileExtensions.get(extensionName);
        if (handler != null)
            return handler.apply(open(subFile));
        return subFile;
    }

    protected abstract InputStream open(T subFile);

    protected abstract T createSubFile(String fileName);

    public Set<String> list() {
        return listFileName().map(s -> s.substring(name.length() + 1)).collect(Collectors.toSet());
    }

    protected abstract Stream<String> listFileName();

    @Override
    public Iterator<T> iterator() {
        return listFileName().map(this::createSubFile).iterator();
    }
}
