package com.github.leeonky.dal.extensions.basic.file.util;

import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.util.BeanClass;

import java.util.Set;

public class FileGroupJavaClassPropertyAccessor extends JavaClassPropertyAccessor<FileGroup> {

    public FileGroupJavaClassPropertyAccessor() {
        super(BeanClass.create(FileGroup.class));
    }

    @Override
    public Object getValue(FileGroup fileGroup, Object name) {
        return fileGroup.getFile(name);
    }

    @Override
    public Set<Object> getPropertyNames(FileGroup fileGroup) {
        return fileGroup.list();
    }
}
