package com.github.leeonky.dal.extensions.basic.zip;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.zip.util.ZipBinary;
import com.github.leeonky.dal.extensions.basic.zip.util.ZipBinaryJavaClassPropertyAccessor;
import com.github.leeonky.dal.extensions.basic.zip.util.ZipNodeJavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.inspector.Inspector;
import com.github.leeonky.dal.runtime.inspector.InspectorContext;

import java.util.stream.Collectors;

import static com.github.leeonky.dal.extensions.basic.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.basic.FileGroup.register;

public class ZipExtension implements Extension {
    private static final Inspector ZIP_BINARY_INSPECTOR = new ZipBinaryInspector();

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerStaticMethodExtension(StaticMethods.class)
                .registerImplicitData(ZipBinary.ZipNode.class, ZipBinary.ZipNode::open)
                .registerPropertyAccessor(ZipBinary.class, new ZipBinaryJavaClassPropertyAccessor())
                .registerPropertyAccessor(ZipBinary.ZipNode.class, new ZipNodeJavaClassPropertyAccessor())
                .registerInspector(ZipBinary.class, data -> ZIP_BINARY_INSPECTOR)
                .registerInspector(ZipBinary.ZipNode.class, data -> new ZipNodeInspector())
        ;

        register("zip", inputStream -> new ZipBinary(readAll(inputStream)));
        register("ZIP", inputStream -> new ZipBinary(readAll(inputStream)));
    }

    public static class StaticMethods {
        public static ZipBinary unzip(byte[] data) {
            return new ZipBinary(data);
        }
    }

    public static class ZipBinaryInspector implements Inspector {

        @Override
        public String inspect(Data data, InspectorContext context) {
            return ("zip file\n" + data.getDataList().stream().map(Data::dump).collect(Collectors.joining("\n"))).trim();
        }
    }

    public static class ZipNodeInspector implements Inspector {

        @Override
        public String inspect(Data data, InspectorContext context) {
            ZipBinary.ZipNode node = (ZipBinary.ZipNode) data.getInstance();
            return String.format("%s %6s %s", node.lastModifiedTime(), node.getSize(), node.name());
        }
    }
}
