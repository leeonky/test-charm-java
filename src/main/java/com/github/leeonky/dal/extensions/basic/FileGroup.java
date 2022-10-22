package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.runtime.PartialObject;
import com.github.leeonky.util.Suppressor;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public abstract class FileGroup<T> implements PartialObject, Iterable<T> {
    protected static final Map<String, Function<InputStream, Object>> fileExtensions = new HashMap<>();
    protected final String name;

    public FileGroup(String name) {
        this.name = name;
    }

    public static void register(String fileExtension, Function<InputStream, Object> fileReader) {
        fileExtensions.put(fileExtension, fileReader);
    }

    @Override
    public String buildField(Object prefix, Object postfix) {
        return fileName(postfix);
    }

    protected String fileName(Object fileExtension) {
        return String.format("%s.%s", name, fileExtension);
    }

    public Object getFile(Object extensionName) {
        T subFile = createSubFile(fileName(extensionName));
        Function<InputStream, Object> handler = fileExtensions.get(extensionName);
        if (handler != null)
            return Suppressor.get(() -> {
                try (InputStream open = open(subFile)) {
                    return handler.apply(open);
                }
            });
        return subFile;
    }

    protected abstract InputStream open(T subFile);

    protected abstract T createSubFile(String fileName);

    public Set<String> list() {
        return listFileNameWithOrder().map(s -> s.substring(name.length() + 1)).collect(toCollection(LinkedHashSet::new));
    }

    protected abstract Stream<String> listFileName();

    @Override
    public Iterator<T> iterator() {
        return listFileNameWithOrder().map(this::createSubFile).iterator();
    }

    private Stream<String> listFileNameWithOrder() {
        return listFileName().sorted();
    }
}
