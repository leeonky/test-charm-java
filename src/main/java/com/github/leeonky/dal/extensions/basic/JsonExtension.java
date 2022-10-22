package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import org.json.JSONArray;

import static com.github.leeonky.dal.extensions.basic.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.basic.FileGroup.register;
import static com.github.leeonky.dal.extensions.basic.JsonExtension.StaticMethods.json;

public class JsonExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);

        register("json", inputStream -> json(readAll(inputStream)));
        register("JSON", inputStream -> json(readAll(inputStream)));
    }

    public static class StaticMethods {
        public static Object json(byte[] data) {
            return json(new String(data));
        }

        public static Object json(CharSequence data) {
            return new JSONArray("[" + data + "]").toList().get(0);
        }
    }
}
