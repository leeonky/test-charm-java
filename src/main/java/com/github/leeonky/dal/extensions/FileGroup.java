package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.runtime.Flatten;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class FileGroup implements Flatten {
    private final File folder;
    private final String name;
    private static final Map<String, Function<File, Object>> fileExtensions = new HashMap<>();

    static {
        register("txt", StringExtension.StaticMethods::string);
        register("TXT", StringExtension.StaticMethods::string);
    }

    public static void register(String txt, Function<File, Object> fileReader) {
        fileExtensions.put(txt, fileReader);
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
//                            TODO return default file when no extension static method
        return Collections.emptyList();
    }

    private String fileName(Object property) {
        return String.format("%s.%s", name, property);
    }

    public Object getFile(String name) {
        String fileName = fileName(name);
        if (!new File(folder, fileName).exists())
            throw new IllegalArgumentException(String.format("File `%s` not exist", fileName));
        return fileExtensions.getOrDefault(name, file -> file).apply(new File(folder, fileName));
    }
}
