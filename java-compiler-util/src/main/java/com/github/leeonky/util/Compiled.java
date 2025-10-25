package com.github.leeonky.util;

import lombok.SneakyThrows;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class Compiled {
    private final String name;
    private final String code;
    private URLClassLoader classLoader;
    private final String packagePrefix;
    private Class<?> clazz;

    public Compiled(String name, String code, String packagePrefix) {
        this.name = name;
        this.code = code;
        this.packagePrefix = packagePrefix;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        if (clazz == null)
            clazz = Sneaky.get(() -> Class.forName(packagePrefix + name, true, getUrlClassLoader()));
        return clazz;
    }

    @SneakyThrows
    private URLClassLoader getUrlClassLoader() {
        return URLClassLoader.newInstance(new URL[]{new File("").toURI().toURL()});
    }
}
