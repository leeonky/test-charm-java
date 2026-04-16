package org.testcharm.message;

import org.json.JSONArray;

import static java.util.Collections.singleton;
import static org.testcharm.util.BeanClass.normalize;

public class JsonMessageConverter implements MessageConverter {

    @Override
    public String serialize(Object obj) {
        String json = new JSONArray(singleton(normalize(obj))).toString();
        return json.substring(1, json.length() - 1);
    }

    @Override
    public Object deserialize(String str) {
        return new JSONArray("[" + str + "]").toList().get(0);
    }
}
