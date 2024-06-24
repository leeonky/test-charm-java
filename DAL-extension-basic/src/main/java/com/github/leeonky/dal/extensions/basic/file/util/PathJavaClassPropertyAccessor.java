package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.util.BeanClass;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

public class PathJavaClassPropertyAccessor extends JavaClassPropertyAccessor<Path> {

    public PathJavaClassPropertyAccessor() {
        super(BeanClass.create(Path.class));
    }

    @Override
    public Set<Object> getPropertyNames(Path path) {
        File file = path.toFile();
        return file.isDirectory() ? Util.listFileNames(file) : super.getPropertyNames(path);
    }

    @Override
    public Object getValue(Path path, Object name) {
        File file = path.toFile();
        return file.isDirectory() ? Util.getSubFile(file, (String) name) : super.getValue(path, name);
    }
}
