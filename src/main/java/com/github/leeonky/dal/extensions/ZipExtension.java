package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.util.Suppressor;

import java.io.File;
import java.util.zip.ZipFile;

public class ZipExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerStaticMethodExtension(StaticMethods.class)
                .registerImplicitData(ZipFileTree.ZipNode.class, ZipFileTree.ZipNode::getBinary)
        ;
    }

    public static class StaticMethods {
        public static ZipFileTree unzip(File file) {
            return Suppressor.get(() -> new ZipFileTree(new ZipFile(file)));
        }
    }
}
