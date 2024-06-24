package com.github.leeonky.dal.extensions.basic.zip;

import com.github.leeonky.dal.extensions.basic.zip.util.ZipBinary;

public class Methods {
    public static ZipBinary unzip(byte[] data) {
        return new ZipBinary(data);
    }
}
