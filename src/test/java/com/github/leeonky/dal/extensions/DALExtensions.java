package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import lombok.SneakyThrows;

public class DALExtensions implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder runtimeContextBuilder = dal.getRuntimeContextBuilder();
        runtimeContextBuilder.registerStaticMethodExtension(StaticMethods.class);
    }

    public static class StaticMethods {

        @SneakyThrows
        public static Object string(byte[] body) {
            return new String(body);
        }
    }
}
