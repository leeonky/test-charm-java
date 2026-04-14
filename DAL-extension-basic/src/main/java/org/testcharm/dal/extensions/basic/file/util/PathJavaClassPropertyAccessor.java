package org.testcharm.dal.extensions.basic.file.util;

import org.testcharm.dal.runtime.JavaClassPropertyAccessor;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

public class PathJavaClassPropertyAccessor extends JavaClassPropertyAccessor<Path> {

    @Override
    public Set<?> getPropertyNames(Path path) {
        File file = path.toFile();
        return file.isDirectory() ? Util.listFileNames(file) : super.getPropertyNames(path);
    }

    @Override
    public Object getValue(Path path, Object name) {
        File file = path.toFile();
        return file.isDirectory() ? Util.getSubFile(file, (String) name) : super.getValue(path, name);
    }
}
