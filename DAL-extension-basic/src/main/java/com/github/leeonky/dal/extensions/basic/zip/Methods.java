package com.github.leeonky.dal.extensions.basic.zip;

import com.github.leeonky.dal.extensions.basic.zip.util.ZipBinary;
import com.github.leeonky.util.Suppressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.github.leeonky.dal.extensions.basic.binary.BinaryExtension.readAllAndClose;

public class Methods {
    public static ZipBinary unzip(byte[] data) {
        return new ZipBinary(data);
    }

    public static byte[] gzip(byte[] data) {
        return Suppressor.get(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(outputStream)) {
                gzipOut.write(data);
            }
            return outputStream.toByteArray();
        });
    }

    public static byte[] ungzip(byte[] data) {
        return Suppressor.get(() -> readAllAndClose(new GZIPInputStream(new ByteArrayInputStream(data))));
    }

    public static byte[] gzip(String data) {
        return gzip(data.getBytes());
    }
}
