package com.github.leeonky.dal.extensions.basic.binary.util;

import com.github.leeonky.dal.runtime.BuildInTextFormatter;
import com.github.leeonky.dal.runtime.TextAttribute;

import static com.github.leeonky.dal.extensions.basic.binary.util.Hex.parseBinary;

public class HexFormatter extends BuildInTextFormatter {

    @Override
    public String description() {
        return "use hex numbers as binary data, like 'FF EF 08...'";
    }

    @Override
    public Object format(Object content, TextAttribute attribute) {
        return parseBinary((String) content);
    }
}
