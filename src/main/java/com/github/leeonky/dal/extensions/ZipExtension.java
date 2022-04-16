package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.util.Set;

import static com.github.leeonky.dal.extensions.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.FileGroup.register;
import static com.github.leeonky.util.BeanClass.create;

public class ZipExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerStaticMethodExtension(StaticMethods.class)
                .registerImplicitData(ZipBinary.ZipNode.class, ZipBinary.ZipNode::open)
                .registerPropertyAccessor(ZipBinary.class,
                        new JavaClassPropertyAccessor<ZipBinary>(runtimeContextBuilder,
                                create(ZipBinary.class)) {

                            @Override
                            public Object getValue(ZipBinary zipBinaryTree, String name) {
                                return zipBinaryTree.getSub(name);
                            }

                            @Override
                            public Set<String> getPropertyNames(ZipBinary zipBinaryTree) {
                                return zipBinaryTree.list();
                            }
                        })
                .registerPropertyAccessor(ZipBinary.ZipNode.class,
                        new JavaClassPropertyAccessor<ZipBinary.ZipNode>(runtimeContextBuilder,
                                create(ZipBinary.ZipNode.class)) {

                            @Override
                            public Object getValue(ZipBinary.ZipNode zipNode, String name) {
                                if (zipNode.isDirectory())
                                    return zipNode.getSub(name);
                                return super.getValue(zipNode, name);
                            }

                            @Override
                            public Set<String> getPropertyNames(ZipBinary.ZipNode zipNode) {
                                if (zipNode.isDirectory())
                                    return zipNode.list();
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
