package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.github.leeonky.dal.extensions.basic.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.basic.FileGroup.register;
import static com.github.leeonky.util.BeanClass.create;

public class ZipExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerStaticMethodExtension(StaticMethods.class)
                .registerImplicitData(ZipBinary.ZipNode.class, ZipBinary.ZipNode::open)
                .registerPropertyAccessor(ZipBinary.class,
                        new JavaClassPropertyAccessor<ZipBinary>(create(ZipBinary.class)) {

                            @Override
                            public Object getValue(ZipBinary zipBinaryTree, Object name) {
                                return zipBinaryTree.getSub((String) name);
                            }

                            @Override
                            public Set<Object> getPropertyNames(ZipBinary zipBinaryTree) {
                                return new LinkedHashSet<>(zipBinaryTree.list());
                            }
                        })
                .registerPropertyAccessor(ZipBinary.ZipNode.class,
                        new JavaClassPropertyAccessor<ZipBinary.ZipNode>(create(ZipBinary.ZipNode.class)) {

                            @Override
                            public Object getValue(ZipBinary.ZipNode zipNode, Object name) {
                                if (zipNode.isDirectory())
                                    return zipNode.getSub((String) name);
                                return super.getValue(zipNode, name);
                            }

                            @Override
                            public Set<Object> getPropertyNames(ZipBinary.ZipNode zipNode) {
                                if (zipNode.isDirectory())
                                    return new LinkedHashSet<>(zipNode.list());
                                return super.getPropertyNames(zipNode);
                            }
                        })
        ;

        register("zip", inputStream -> new ZipBinary(readAll(inputStream)));
        register("ZIP", inputStream -> new ZipBinary(readAll(inputStream)));
    }

    public static class StaticMethods {

        public static ZipBinary unzip(byte[] data) {
            return new ZipBinary(data);
        }
    }
}
