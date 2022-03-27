package com.github.leeonky.dal.extensions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

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
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int size;
                byte[] data = new byte[1024];
                while ((size = stream.read(data, 0, data.length)) != -1)
                    buffer.write(data, 0, size);
                return json(buffer.toByteArray());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        public static Object json(File file) {
            try {
                return json(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        public static Object json(Path path) {
            return json(path.toFile());
        }
    }
}
