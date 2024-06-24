package com.github.leeonky.dal.extensions.basic.file;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Methods {
    public static File file(String path) {
        return Paths.get(path).toFile();
    }

    public static Path path(String path) {
        return Paths.get(path);
    }
}
