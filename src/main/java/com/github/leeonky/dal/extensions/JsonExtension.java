package com.github.leeonky.dal.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.util.Suppressor;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static com.github.leeonky.dal.extensions.BinaryExtension.StaticMethods.binary;

public class JsonExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);
    }

    public static class StaticMethods {
        public static Object json(byte[] data) {
            return json(new String(data));
        }

        public static Object json(String data) {
            return Suppressor.get(() -> new ObjectMapper().readValue("[" + data + "]", List.class).get(0));
        }

        public static Object json(InputStream stream) {
            return json(binary(stream));
        }

        public static Object json(File file) {
            return json(binary(file));
        }

        public static Object json(Path path) {
            return json(binary(path));
        }
    }
}
