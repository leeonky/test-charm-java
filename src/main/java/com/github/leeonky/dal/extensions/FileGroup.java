package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.runtime.Flatten;
import com.github.leeonky.util.Suppressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FileGroup implements Flatten {
    private final File folder;
    private final String name;

    public FileGroup(File folder, String name) {
        this.folder = folder;
        this.name = name;
    }

    @Override
    public List<String> removeExpectedFields(Set<String> fields, Object symbol, Object property) {
        String fileName = String.format("%s.%s", symbol, property);
        if (fields.contains(fileName)) {
            fields.remove(fileName);
            return Collections.singletonList(fileName);
        }
//        TODO raiser error file not found
//                            TODO return default file when no extension static method
        return Collections.emptyList();
    }

    public InputStream getStream(String extension) {
        return Suppressor.get(() -> new FileInputStream(new File(folder, String.format("%s.%s", name, extension))));
    }
}
