package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

import java.io.*;
import java.nio.file.Path;

public class BinaryExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);
    }

    public static class StaticMethods {

        public static byte[] binary(InputStream stream) {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int size;
                byte[] data = new byte[1024];
                while ((size = stream.read(data, 0, data.length)) != -1)
                    buffer.write(data, 0, size);
                return buffer.toByteArray();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public static byte[] binary(File file) {
            try {
                return binary(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        public static byte[] binary(Path path) {
            return binary(path.toFile());
        }
    }
}
