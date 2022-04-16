package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.JavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.util.Suppressor;

import java.io.File;
import java.util.Set;
import java.util.zip.ZipFile;

import static com.github.leeonky.util.BeanClass.create;

public class ZipExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerStaticMethodExtension(StaticMethods.class)
                .registerImplicitData(ZipFileTree.ZipNode.class, ZipFileTree.ZipNode::open)
                .registerPropertyAccessor(ZipFileTree.class,
                        new JavaClassPropertyAccessor<ZipFileTree>(runtimeContextBuilder,
                                create(ZipFileTree.class)) {

                            @Override
                            public Object getValue(ZipFileTree zipFileTree, String name) {
                                return zipFileTree.getSub(name);
                            }

                            @Override
                            public Set<String> getPropertyNames(ZipFileTree zipFileTree) {
                                return zipFileTree.list();
                            }
                        })
                .registerPropertyAccessor(ZipFileTree.ZipNode.class,
                        new JavaClassPropertyAccessor<ZipFileTree.ZipNode>(runtimeContextBuilder,
                                create(ZipFileTree.ZipNode.class)) {

                            @Override
                            public Object getValue(ZipFileTree.ZipNode zipNode, String name) {
                                if (zipNode.isDirectory())
                                    return zipNode.getSub(name);
                                return super.getValue(zipNode, name);
                            }

                            @Override
                            public Set<String> getPropertyNames(ZipFileTree.ZipNode zipNode) {
                                if (zipNode.isDirectory())
                                    return zipNode.list();
                                return super.getPropertyNames(zipNode);
                            }
                        })
        ;
    }

    public static class StaticMethods {

        public static ZipFileTree unzip(File file) {
            return Suppressor.get(() -> new ZipFileTree(new ZipFile(file)));
        }
    }
}
