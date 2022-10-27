package com.github.leeonky.dal.extensions.basic.zip.util;

import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.util.BeanClass;

import java.util.LinkedHashSet;
import java.util.Set;

public class ZipBinaryJavaClassPropertyAccessor extends JavaClassPropertyAccessor<ZipBinary> {

    public ZipBinaryJavaClassPropertyAccessor() {
        super(BeanClass.create(ZipBinary.class));
    }

    @Override
    public Object getValue(ZipBinary zipBinaryTree, Object name) {
        return zipBinaryTree.getSub((String) name);
    }

    @Override
    public Set<Object> getPropertyNames(ZipBinary zipBinaryTree) {
        return new LinkedHashSet<>(zipBinaryTree.list());
    }
}
