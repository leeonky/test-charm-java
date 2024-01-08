package com.github.leeonky.dal.extensions.basic.text;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.TextAttribute;
import com.github.leeonky.dal.runtime.TextFormatter;

import static com.github.leeonky.dal.extensions.basic.binary.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.basic.file.util.FileGroup.register;
import static com.github.leeonky.dal.extensions.basic.text.Methods.json;

@SuppressWarnings("unused")
public class JsonExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder builder = dal.getRuntimeContextBuilder();
        builder.registerStaticMethodExtension(Methods.class);

        builder.registerTextFormatter("JSON", jsonTextFormatter("JSON"));
        builder.registerTextFormatter("json", jsonTextFormatter("json"));

        register("json", inputStream -> json(readAll(inputStream)));
        register("JSON", inputStream -> json(readAll(inputStream)));
    }

    private TextFormatter<String, Object> jsonTextFormatter(String label) {
        return new TextFormatter<String, Object>() {
            @Override
            protected Object format(String content, TextAttribute attribute, DALRuntimeContext context) {
                return json(content);
            }

            @Override
            public String description() {
                return "use " + label + " as json object";
            }
        };
    }
}
