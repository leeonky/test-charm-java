package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.runtime.Flatten;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.github.leeonky.dal.extensions.BinaryExtension.StaticMethods.binary;

public class FileGroup implements Flatten {
    private final File file;
    private final String name;

    public FileGroup(File file, String name) {
        this.file = file;
        this.name = name;
    }

    @Override
    public List<String> removeExpectedFields(Set<String> fields, Object symbol, Object property) {
        String fileName = toName(symbol);
        if (fields.contains(fileName)) {
            fields.remove(fileName);
            return Collections.singletonList(fileName);
        }
//        TODO raiser error file not found
        return Collections.emptyList();
    }

    private String toName(Object symbol) {
        return String.format("%s.%s", name, symbol);
    }

    public byte[] getBinary(String extension) {
        return binary(new File(file, toName(extension)));
    }
}
