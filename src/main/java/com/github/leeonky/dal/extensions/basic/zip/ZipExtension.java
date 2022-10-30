package com.github.leeonky.dal.extensions.basic.zip;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.zip.util.*;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import static com.github.leeonky.dal.extensions.basic.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.basic.FileGroup.register;

public class ZipExtension implements Extension {
    private static final ZipBinaryDumper ZIP_BINARY_DUMPER = new ZipBinaryDumper();
    private static final ZipNodeDumper ZIP_NODE_DUMPER = new ZipNodeDumper();

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerStaticMethodExtension(StaticMethods.class)
                .registerImplicitData(ZipBinary.ZipNode.class, ZipBinary.ZipNode::open)
                .registerPropertyAccessor(ZipBinary.class, new ZipBinaryJavaClassPropertyAccessor())
                .registerPropertyAccessor(ZipBinary.ZipNode.class, new ZipNodeJavaClassPropertyAccessor())
                .registerDumper(ZipBinary.class, data -> ZIP_BINARY_DUMPER)
                .registerDumper(ZipBinary.ZipNode.class, data -> ZIP_NODE_DUMPER)
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
