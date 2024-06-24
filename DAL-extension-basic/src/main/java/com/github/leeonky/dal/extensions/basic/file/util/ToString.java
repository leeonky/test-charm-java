package com.github.leeonky.dal.extensions.basic.file.util;

import java.nio.file.Path;

public class ToString {
    public static String name(Path path) {
        return path.toFile().getName();
    }
}
