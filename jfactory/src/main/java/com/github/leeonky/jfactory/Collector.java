package com.github.leeonky.jfactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.leeonky.util.function.Extension.mapValue;

public class Collector {
    private final JFactory jFactory;
    private final Class<?> defaultType;
    private final LinkedHashMap<String, Collector> fields = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, Collector> list = new LinkedHashMap<>();
    private Object value;
    private Type type = Type.OBJECT;
    private boolean isSpecifySpec = false;
    private boolean raw = false;
    private String[] traitsSpec;
    private boolean intently = false;

    protected Collector(JFactory jFactory, Class<?> defaultType) {
        this.jFactory = jFactory;
        this.defaultType = defaultType;
    }

    protected Collector(JFactory jFactory, String... traitsSpec) {
        this(jFactory, Object.class);
        this.traitsSpec = traitsSpec;
    }

    public Object build() {
        if (traitsSpec() == null || raw) {
            if (defaultType.equals(Object.class) || raw) {
                switch (type) {
                    case VALUE:
                        return value;
                    case LIST: {
                        Object[] list = new Object[this.list.isEmpty() ? 0 : (Collections.max(this.list.keySet()) + 1)];
                        this.list.forEach((key, value) -> list[key] = value.build());
                        return list;
                    }
                }
                return mapValue(fields, Collector::build, LinkedHashMap::new);
            }
        }
        return builder().properties(properties()).create();
    }

    @SuppressWarnings("unchecked")
    public Map<String, ?> properties() {
        Object o = objectValue();
        return o instanceof FlatAble ? ((FlatAble) o).flat() : (Map<String, ?>) o;
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
        isSpecifySpec = true;
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
        if (raw)
            return build();
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

    public void raw() {
        if (isSpecifySpec)
            throw new IllegalStateException("Cannot create raw Map/List when traits were specified");
        raw = true;
    }

    public void intently() {
        intently = true;
    }

    class ObjectValue extends LinkedHashMap<String, Object> implements FlatAble {
        public <K> ObjectValue(Map<K, Collector> data, Function<K, String> keyMapper) {
            data.forEach((key, value) -> put(keyMapper.apply(key), value.objectValue()));
        }

        @Override
        public String buildPropertyName(String property) {
            if (traitsSpec != null)
                property += "(" + String.join(" ", traitsSpec) + ")";
            if (intently)
                property += "!";
            return property;
        }
    }

    public enum Type {
        LIST, OBJECT, VALUE
    }
}

interface FlatAble {

    default Map<String, Object> flat() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        forEach((key, value) -> {
            if (value instanceof FlatAble)
                ((FlatAble) value).flatSub(map, key);
            else if (value instanceof Map && ((Map<?, ?>) value).isEmpty())
                map.put(key + "(EMPTY_MAP)", value);
            else
                map.put(key, value);
        });
        return map;
    }

    default String buildPropertyName(String property) {
        return property;
    }

    void forEach(BiConsumer<? super String, ? super Object> action);

    default void flatSub(LinkedHashMap<String, Object> result, String key) {
        Map<String, Object> flat = flat();
        if (flat.isEmpty())
            result.put(buildPropertyName(key), flat);
        else
            flat.forEach((subKey, subValue) -> result.put(buildPropertyName(key) +
                    (subKey.startsWith("[") ? subKey : "." + subKey), subValue));
    }
}
