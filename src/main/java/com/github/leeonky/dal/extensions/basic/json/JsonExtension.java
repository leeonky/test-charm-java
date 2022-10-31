package com.github.leeonky.dal.extensions.basic.json;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

import static com.github.leeonky.dal.extensions.basic.binary.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.basic.file.util.FileGroup.register;
import static com.github.leeonky.dal.extensions.basic.json.Methods.json;

@SuppressWarnings("unused")
public class JsonExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(Methods.class);

        register("json", inputStream -> json(readAll(inputStream)));
        register("JSON", inputStream -> json(readAll(inputStream)));
    }
}
