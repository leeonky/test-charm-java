package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.PartialObject;
import com.github.leeonky.dal.runtime.ProxyObject;
import com.github.leeonky.util.Sneaky;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public abstract class FileGroup<T> implements PartialObject, Iterable<T>, ProxyObject {
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

    protected abstract InputStream open(T subFile);

    protected abstract T createSubFile(String fileName);

    protected abstract Stream<String> listFileName();

    @Override
    public Iterator<T> iterator() {
        return listFileNameWithOrder().map(this::createSubFile).iterator();
    }

    private Stream<String> listFileNameWithOrder() {
        return listFileName().sorted();
    }

    public Object getFile(Object property) {
        T subFile = createSubFile(fileName(property));
        Function<InputStream, Object> handler = fileExtensions.get(property);
        if (handler != null)
            return Sneaky.get(() -> {
                    try (InputStream open = open(subFile)) {
                        return handler.apply(open);
                    }
                });
        return subFile;
    }

    public Set<String> list() {
        return listFileNameWithOrder().map(s -> s.substring(name.length() + 1))
                .collect(toCollection(LinkedHashSet::new));
    }

    @Override
    public Object getValue(Object property) {
        return getFile(property);
    }

    @Override
    public Set<?> getPropertyNames() {
        return list();
    }
}
