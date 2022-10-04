package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.util.Suppressor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
                .registerStaticMethodExtension(StaticMethods.class)
                .registerImplicitData(InputStream.class, BinaryExtension::readAllAndClose);
    }

    public static class StaticMethods {
        public static byte[] binary(byte[] bytes) {
            return bytes;
        }

        public static String base64(byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        }

        public static String decode(byte[] bytes, String decoder) throws UnsupportedEncodingException {
            return new String(bytes, decoder);
        }

        public static String utf8(byte[] bytes) {
            return new String(bytes);
        }

        public static String ascii(byte[] bytes) {
            return new String(bytes, StandardCharsets.US_ASCII);
        }

        public static String iso8859_1(byte[] bytes) {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        }

        public static String gbk(byte[] bytes) throws UnsupportedEncodingException {
            return new String(bytes, "gbk");
        }
    }
}
