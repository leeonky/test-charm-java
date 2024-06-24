package com.github.leeonky.dal.extensions.basic.string;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static java.util.Arrays.asList;

public class Methods {
    private static final List<String> SPLITTERS = asList("\r\n", "\n\r", "\n", "\r");

    public static String string(byte[] data) {
        return new String(data);
    }

    public static List<String> lines(byte[] content) {
        return lines(new String(content));
    }

    public static List<String> lines(String content) {
        return lines(content, new ArrayList<>());
    }

    private static List<String> lines(String content, List<String> list) {
        for (String str : SPLITTERS) {
            int index = content.indexOf(str);
            if (index != -1) {
                lines(content.substring(0, index), list);
                return lines(content.substring(index + str.length()), list);
            }
        }
        list.add(content);
        return list;
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
