package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.github.leeonky.dal.extensions.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.FileGroup.register;
import static com.github.leeonky.dal.extensions.StringExtension.StaticMethods.string;
import static java.util.Arrays.asList;

public class StringExtension implements Extension {
    private static final List<String> SPLITTERS = asList("\r\n", "\n\r", "\n", "\r");

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);

        register("txt", inputStream -> string(readAll(inputStream)));
        register("TXT", inputStream -> string(readAll(inputStream)));
    }

    public static class StaticMethods {
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

        public static Object encode(String content, String encoder) throws UnsupportedEncodingException {
            return content.getBytes(encoder);
        }
    }
}
