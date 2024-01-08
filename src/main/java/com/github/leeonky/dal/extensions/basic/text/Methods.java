package com.github.leeonky.dal.extensions.basic.text;

import org.json.JSONArray;

public class Methods {
    public static Object json(byte[] data) {
        return json(new String(data));
    }

    public static Object json(CharSequence data) {
        return new JSONArray("[" + data + "]").toList().get(0);
    }
}
