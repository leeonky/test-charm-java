package com.github.leeonky.dal.extensions.basic.zip.util;

import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.util.BeanClass;

import java.util.Set;

public class ZipNodeJavaClassPropertyAccessor extends JavaClassPropertyAccessor<ZipBinary.ZipNode> {
    public ZipNodeJavaClassPropertyAccessor() {
        super(BeanClass.create(ZipBinary.ZipNode.class));
    }

    @Override
    public Object getValue(ZipBinary.ZipNode zipNode, Object name) {
        if (zipNode.isDirectory())
            return zipNode.getValue(name);
        return super.getValue(zipNode, name);
    }

    @Override
    public Set<?> getPropertyNames(ZipBinary.ZipNode zipNode) {
        if (zipNode.isDirectory())
            return zipNode.getPropertyNames();
        return super.getPropertyNames(zipNode);
    }
}
