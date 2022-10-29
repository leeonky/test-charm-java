package com.github.leeonky.dal.extensions;

import com.github.leeonky.cucumber.restful.RestfulStep;
import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import lombok.SneakyThrows;

public class UploadFileExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);
    }

    public static class StaticMethods {

        @SneakyThrows
        public static byte[] binaryContent(RestfulStep.UploadFile uploadFile) {
            return uploadFile.getContent();
        }
    }
}
