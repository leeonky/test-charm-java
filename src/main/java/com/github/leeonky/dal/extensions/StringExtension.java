package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

import static com.github.leeonky.dal.extensions.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.FileGroup.register;
import static com.github.leeonky.dal.extensions.StringExtension.StaticMethods.string;

public class StringExtension implements Extension {

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
    }
}
