package com.github.leeonky.dal.extensions.basic.text;

import com.github.leeonky.dal.extensions.basic.string.jsonsource.org.json.JSONArray;
import org.yaml.snakeyaml.Yaml;

public class Methods {
    public static Object json(byte[] data) {
        return json(new String(data));
    }

    public static Object json(CharSequence data) {
        return new JSONArray("[" + data + "]").toList().get(0);
    }

    public static Object yaml(byte[] data) {
        return yaml(new String(data));
    }

    public static Object yaml(CharSequence data) {
        return new Yaml().load(data.toString());
    }
}
