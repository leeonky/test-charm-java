package org.testcharm.dal.extensions.basic.zip.util;

import org.testcharm.dal.runtime.JavaClassPropertyAccessor;

import java.util.Set;

public class ZipNodeJavaClassPropertyAccessor extends JavaClassPropertyAccessor<ZipBinary.ZipNode> {

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
