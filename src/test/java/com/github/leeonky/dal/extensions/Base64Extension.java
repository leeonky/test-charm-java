package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Extension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);
    }

    public static class StaticMethods {

        @SneakyThrows
        public static String decodeToStr(String encoded) {
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        }
    }
}
