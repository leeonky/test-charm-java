package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

import static com.github.leeonky.dal.extensions.StringExtension.StaticMethods.string;

public class FileExtensions implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);
    }

    public static class StaticMethods {
        public static String txt(FileGroup fileGroup) {
            return string(fileGroup.getStream("txt"));
        }

        public static String TXT(FileGroup fileGroup) {
            return string(fileGroup.getStream("TXT"));
        }
    }
}
