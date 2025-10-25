package com.github.leeonky.util;

import lombok.SneakyThrows;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;
import java.util.Objects;

public class Definition extends SimpleJavaFileObject {
    private final String packageName;
    private final String simpleName;
    private final String mainCode;

    Definition(String packageName, String simpleName, String mainCode) {
        super(URI.create("string:///" + (packageName + "." + simpleName).replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.mainCode = mainCode;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return "package " + packageName + ";\n" + mainCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Definition.class, packageName, mainCode);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Definition && Objects.equals(packageName, ((Definition) o).packageName)
                && Objects.equals(mainCode, ((Definition) o).mainCode);
    }

    public String getSimpleClassName() {
        return simpleName;
    }

    @SneakyThrows
    public Class<?> loadClass(ClassLoader classLoader) {
        return classLoader.loadClass(packageName + "." + simpleName);
    }
}
