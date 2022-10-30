package com.github.leeonky.dal.extensions.basic.zip;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.zip.util.ZipBinary;
import com.github.leeonky.dal.extensions.basic.zip.util.ZipBinaryJavaClassPropertyAccessor;
import com.github.leeonky.dal.extensions.basic.zip.util.ZipNodeJavaClassPropertyAccessor;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.inspector.DumpingContext;
import com.github.leeonky.dal.runtime.inspector.InspectorBk;
import com.github.leeonky.dal.util.TextUtil;

import static com.github.leeonky.dal.extensions.basic.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.basic.FileGroup.register;
import static java.util.stream.Collectors.joining;

public class ZipExtension implements Extension {
    private static final InspectorBk ZIP_BINARY_INSPECTOR_BK = new ZipBinaryInspectorBk();

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerStaticMethodExtension(StaticMethods.class)
                .registerImplicitData(ZipBinary.ZipNode.class, ZipBinary.ZipNode::open)
                .registerPropertyAccessor(ZipBinary.class, new ZipBinaryJavaClassPropertyAccessor())
                .registerPropertyAccessor(ZipBinary.ZipNode.class, new ZipNodeJavaClassPropertyAccessor())
                .registerInspector(ZipBinary.class, data -> ZIP_BINARY_INSPECTOR_BK)
                .registerInspector(ZipBinary.ZipNode.class, data -> new ZipNodeInspectorBk())
        ;

        register("zip", inputStream -> new ZipBinary(readAll(inputStream)));
        register("ZIP", inputStream -> new ZipBinary(readAll(inputStream)));
    }

    public static class StaticMethods {
        public static ZipBinary unzip(byte[] data) {
            return new ZipBinary(data);
        }
    }

    public static class ZipBinaryInspectorBk implements InspectorBk {

        @Override
        public String inspect(Data data, DumpingContext context) {
            return ("zip archive\n" + data.getDataList().stream().map(Data::dump).collect(joining("\n"))).trim();
        }

        @Override
        public String dump(Data data, DumpingContext context) {
            return data.getDataList().stream().map(Data::dump).map(TextUtil::indent).collect(joining("\n"));
        }
    }

    public static class ZipNodeInspectorBk implements InspectorBk {

        @Override
        public String inspect(Data data, DumpingContext context) {
            ZipBinary.ZipNode node = (ZipBinary.ZipNode) data.getInstance();
            if (node.isDirectory())
                return (node.name() + "/\n" + data.getDataList().stream().map(Data::dump).map(TextUtil::indent)
                        .collect(joining("\n"))).trim();
            if (node.name().toLowerCase().endsWith(".zip")) {
                return (node.name() + "\n" + context.getDalRuntimeContext().wrap(new ZipBinary(readAll(node.open()))).dump()).trim();
            }
            return String.format("%s %6s %s", node.lastModifiedTime(), node.getSize(), node.name());
        }
    }
}
