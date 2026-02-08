package com.github.leeonky.jfactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Collector {
    private final JFactory jFactory;
    private final Class<?> defaultType;
    private final LinkedHashMap<String, Collector> fields = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, Collector> list = new LinkedHashMap<>();
    private Object value;
    private Type type = Type.OBJECT;
    private String[] traitsSpec;

    protected Collector(JFactory jFactory, Class<?> defaultType) {
        this.jFactory = jFactory;
        this.defaultType = defaultType;
    }

    protected Collector(JFactory jFactory, String... traitsSpec) {
        this(jFactory, Object.class);
        setTraitsSpec(traitsSpec);
    }

    public Object build() {
        if (traitsSpec() == null) {
            if (defaultType.equals(Object.class)) {
                switch (type) {
                    case VALUE:
                        return value;
                    case LIST: {
                        Object[] list = new Object[this.list.isEmpty() ? 0 : (Collections.max(this.list.keySet()) + 1)];
                        this.list.forEach((key, value) -> list[key] = value.build());
                        return list;
                    }
                }
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                fields.forEach((key, value) -> map.put(key, value.build()));
                return map;
            }
        }
        return builder().properties(((FlatAble) objectValue()).flat()).create();
    }

    private Builder<?> builder() {
        String[] traitsSpec = traitsSpec();
        return traitsSpec != null ? jFactory.spec(traitsSpec) : jFactory.type(defaultType);
    }

    public void setValue(Object value) {
        this.value = value;
        forceType(Type.VALUE);
    }

    public Collector setTraitsSpec(String[] traitsSpec) {
        this.traitsSpec = traitsSpec;
        return this;
    }

    public String[] traitsSpec() {
        return traitsSpec;
    }

    public Collector collect(int index) {
        forceType(Type.LIST);
        return list.computeIfAbsent(index, k -> jFactory.collector());
    }

    public Collector collect(Object property) {
        return fields.computeIfAbsent((String) property, k -> jFactory.collector());
    }

    private Object objectValue() {
        switch (type) {
            case VALUE:
                return value;
            case LIST:
                return new ObjectValue(list, k -> "[" + k + "]");
            default:
                return new ObjectValue(fields, Function.identity());
        }
    }

    public void forceType(Type type) {
        this.type = type;
    }

    class ObjectValue extends LinkedHashMap<String, Object> implements FlatAble {
        public <K> ObjectValue(Map<K, Collector> data, Function<K, String> keyMapper) {
            data.forEach((key, value) -> put(keyMapper.apply(key), value.objectValue()));
        }
    }

    public enum Type {
        LIST, OBJECT, VALUE;
    }
}

interface FlatAble {

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
