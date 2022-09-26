package com.github.leeonky.dal.extensions;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class TextExtension {
    private static final List<String> SPLITTERS = asList("\r\n", "\n\r", "\n", "\r");

    public static class StaticMethods {
        public static List<String> lines(byte[] content) {
            return lines(new String(content));
        }

        public static List<String> lines(String content) {
            return TextExtension.lines(content, new ArrayList<>());
        }
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
}
