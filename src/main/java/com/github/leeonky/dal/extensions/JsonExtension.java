package com.github.leeonky.dal.extensions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

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
            String str = new String(data);
            return json(str);
        }

        public static Object json(String data) {
            try {
                return new ObjectMapper().readValue("[" + data + "]", List.class).get(0);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
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
