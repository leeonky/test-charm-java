package com.github.leeonky.jfactory.helper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public interface FlatAble {
    default Map<String, Object> flat() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        forEach((key, value) -> {
            if (value instanceof FlatAble) {
                ((FlatAble) value).flatSub(result, key);
            } else
                result.put(key, value);
        });
        return result;
    }

    default String buildPropertyName(String property) {
        return property;
    }

    void forEach(BiConsumer<? super String, ? super Object> action);

    default void flatSub(LinkedHashMap<String, Object> result, String key) {
        for (Map.Entry<String, Object> entry : flat().entrySet())
            result.put(buildPropertyName(key) +
                    (entry.getKey().startsWith("[") ? entry.getKey() : "." + entry.getKey()), entry.getValue());
    }
}
