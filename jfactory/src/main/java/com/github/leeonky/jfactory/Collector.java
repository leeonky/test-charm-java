package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Collector {
    private final JFactory jFactory;
    private final Class<?> defaultType;
    private final LinkedHashMap<String, Collector> fields = new LinkedHashMap<>();
    private final List<Collector> list = new ArrayList<>();
    private Object value;
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
                if (!fields.isEmpty())
                    return asMap();
                if (!list.isEmpty())
                    return asList();
                return value;
            }
        }
        return builder().properties(asMap()).create();
    }

    private List<Object> asList() {
        return list.stream().map(Collector::build).collect(Collectors.toList());
    }

    private Builder<?> builder() {
        String[] traitsSpec = traitsSpec();
        return traitsSpec != null ? jFactory.spec(traitsSpec) : jFactory.type(defaultType);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    private Map<String, Object> asMap() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        fields.forEach((key, value) -> result.put(key, value.build()));
        return result;
    }

    public Collector setTraitsSpec(String[] traitsSpec) {
        this.traitsSpec = traitsSpec;
        return this;
    }

    public String[] traitsSpec() {
        return traitsSpec;
    }

    public Collector collect(int index) {
        int count = index + 1 - list.size();
        while (count-- > 0)
            list.add(null);
        if (list.get(index) == null)
            list.set(index, jFactory.collector());
        return list.get(index);
    }

    public Collector collect(Object property) {
        return fields.computeIfAbsent((String) property, k -> jFactory.collector());
    }
}
