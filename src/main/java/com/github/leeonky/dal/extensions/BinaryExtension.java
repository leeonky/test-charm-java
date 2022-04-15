package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.util.Suppressor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BinaryExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerStaticMethodExtension(StaticMethods.class)
                .registerImplicitData(InputStream.class, BinaryExtension::readAll);
    }

    public static byte[] readAll(InputStream stream) {
        return Suppressor.get(() -> {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int size;
            byte[] data = new byte[1024];
            while ((size = stream.read(data, 0, data.length)) != -1)
                buffer.write(data, 0, size);
            return buffer.toByteArray();
        });
    }

    public static class StaticMethods {

        public static byte[] binary(byte[] bytes) {
            return bytes;
        }
    }
}
