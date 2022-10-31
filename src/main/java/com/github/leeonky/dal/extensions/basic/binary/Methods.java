package com.github.leeonky.dal.extensions.basic.binary;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Methods {
    public static byte[] binary(byte[] bytes) {
        return bytes;
    }

    public static byte[] encode(String content, String encoder) throws UnsupportedEncodingException {
        return content.getBytes(encoder);
    }

    public static byte[] utf8(String content) {
        return content.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] base64(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }

    public static byte[] ascii(String content) {
        return content.getBytes(StandardCharsets.US_ASCII);
    }

    public static byte[] iso8859_1(String content) {
        return content.getBytes(StandardCharsets.ISO_8859_1);
    }

    public static byte[] gbk(String content) throws UnsupportedEncodingException {
        return encode(content, "gbk");
    }
}
