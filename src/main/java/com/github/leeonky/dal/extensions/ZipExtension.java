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
import static java.util.stream.Collectors.toSet;

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
                                return zipFileTree.listNode().stream().map(ZipFileTree.ZipNode::name).collect(toSet());
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
