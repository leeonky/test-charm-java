package org.testcharm.message;

import org.json.JSONArray;
import org.testcharm.util.BeanClass;
import org.testcharm.util.CollectionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonMessageConverter implements MessageConverter {

    @Override
    public String serialize(Object obj) {
        String json = new JSONArray(Collections.singleton(normalize(obj))).toString();
        return json.substring(1, json.length() - 1);
    }

    @SuppressWarnings("unchecked")
    public Object normalize(Object object) {
        if (object == null)
            return null;
        if (object instanceof CharSequence || object instanceof Number || object instanceof Boolean)
            return object;
        BeanClass<Object> type = BeanClass.createFrom(object);
        if (type.isCollection())
            return new ArrayList<Object>() {{
                CollectionHelper.asStream(object).forEach(e -> add(normalize(e)));
            }};
        if (object instanceof Map)
            return new LinkedHashMap<String, Object>() {{
                ((Map) object).forEach((key, value) -> put((String) key, normalize(value)));
            }};
        return new LinkedHashMap<String, Object>() {{
            type.getPropertyReaders().forEach((key, objectPropertyReader) ->
                    put(key, normalize(objectPropertyReader.getValue(object))));
        }};
    }

    @Override
    public Object deserialize(String str) {
        return new JSONArray("[" + str + "]").toList().get(0);
    }
}
