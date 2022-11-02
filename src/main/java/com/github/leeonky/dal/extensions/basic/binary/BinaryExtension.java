package com.github.leeonky.dal.extensions.basic.binary;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.util.Suppressor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BinaryExtension implements Extension {

    public static byte[] readAllAndClose(InputStream stream) {
        try {
            return readAll(stream);
        } finally {
            Suppressor.run(stream::close);
        }
    }

    public static byte[] readAll(InputStream stream) {
        return Suppressor.get(() -> {
            try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                int size;
                byte[] data = new byte[1024];
                while ((size = stream.read(data, 0, data.length)) != -1)
                    buffer.write(data, 0, size);
                return buffer.toByteArray();
            }
        });
    }

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerStaticMethodExtension(Methods.class)
                .registerImplicitData(InputStream.class, BinaryExtension::readAllAndClose);
    }
}
