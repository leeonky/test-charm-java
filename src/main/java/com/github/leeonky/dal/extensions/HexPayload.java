package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.TextAttribute;
import com.github.leeonky.dal.runtime.TextFormatter;

import static com.github.leeonky.dal.extensions.formatters.Hex.hex;

public class HexPayload implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerTextFormatter("HEX", new TextFormatter() {

            @Override
            public String description() {
                return "use hex numbers as binary data, like 'FF EF 08...'";
            }

            @Override
            public Object format(Object content, TextAttribute attribute) {
                return hex((String) content);
            }
        });
    }
}
