package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.extensions.basic.FileGroup;
import com.github.leeonky.util.InvocationException;
import com.github.leeonky.util.Suppressor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.Stream;

public class IOFileFileGroup extends FileGroup<File> {
    private final File folder;

    public IOFileFileGroup(File folder, String name) {
        super(name);
        this.folder = folder;
    }

    @Override
    protected FileInputStream open(File subFile) {
        return Suppressor.get(() -> new FileInputStream(subFile));
    }

    @Override
    protected File createSubFile(String fileName) {
        File subFile = new File(folder, fileName);
        if (!subFile.exists())
            throw new InvocationException(new FileNotFoundException(String.format("File `%s` not exist", fileName)));
        return subFile;
    }

    @Override
    protected Stream<String> listFileName() {
        return Arrays.stream(folder.list()).filter(n -> n.startsWith(name + "."));
    }
}
