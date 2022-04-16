package com.github.leeonky.dal.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.util.Suppressor;

import java.util.List;

import static com.github.leeonky.dal.extensions.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.FileGroup.register;
import static com.github.leeonky.dal.extensions.JsonExtension.StaticMethods.json;

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

        public static Object json(String data) {
            return Suppressor.get(() -> new ObjectMapper().readValue("[" + data + "]", List.class).get(0));
        }
    }
}
