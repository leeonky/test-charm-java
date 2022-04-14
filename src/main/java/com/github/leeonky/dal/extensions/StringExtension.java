package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import static com.github.leeonky.dal.extensions.BinaryExtension.StaticMethods.binary;

public class StringExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);
    }

    public static class StaticMethods {
        public static String string(byte[] data) {
            return new String(data);
        }

        public static String string(InputStream inputStream) {
            return string(binary(inputStream));
        }

        public static String string(File file) {
            return string(binary(file));
        }

        public static String string(Path path) {
            return string(binary(path));
        }

        public static String string(ZipFileTree.ZipNode zipNode) {
            return string(zipNode.getBinary());
        }
    }
}
