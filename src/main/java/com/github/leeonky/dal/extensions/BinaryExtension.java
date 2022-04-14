package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.util.Suppressor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;

public class BinaryExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);
    }

    public static class StaticMethods {

        public static byte[] binary(InputStream stream) {
            return Suppressor.get(() -> {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int size;
                byte[] data = new byte[1024];
                while ((size = stream.read(data, 0, data.length)) != -1)
                    buffer.write(data, 0, size);
                return buffer.toByteArray();
            });
        }

        public static byte[] binary(File file) {
            return Suppressor.get(() -> binary(new FileInputStream(file)));
        }

        public static byte[] binary(Path path) {
            return binary(path.toFile());
        }
    }
}
