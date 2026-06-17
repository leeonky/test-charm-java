package org.testcharm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testcharm.util.function.Extension.mapValue;

public class Collector {
    private final Map<String, Collector> fields = new LinkedHashMap<>();
    private final Map<Integer, Collector> elements = new LinkedHashMap<>();
    private Object value;
    private Type type = Type.OBJECT;

    public Object value() {
        return value;
    }

    public Type type() {
        return type;
    }

    public Map<Integer, ? extends Collector> elements() {
        return elements;
    }

    public Map<String, ? extends Collector> fields() {
        return fields;
    }

    public Object build() {
        switch (type) {
            case VALUE:
                return value;
            case LIST: {
                Object[] list = new Object[elements.isEmpty() ? 0 : (Collections.max(elements.keySet()) + 1)];
                elements.forEach((key, value) -> list[key] = value.build());
                return new ArrayList<>(asList(list));
            }
        }
        return mapValue(fields, Collector::build, LinkedHashMap::new);
    }

    protected Collector createSubCollector() {
        return new Collector();
    }

    public void setValue(Object value) {
        this.value = value;
        type(Type.VALUE);
    }

    public Collector collect(int index) {
        type(Type.LIST);
        return elements.computeIfAbsent(index, k -> createSubCollector());
    }

    public Collector collect(Object property) {
        return fields.computeIfAbsent((String) property, k -> createSubCollector());
    }

    public void type(Type type) {
        this.type = type;
    }

    public enum Type {
        LIST, OBJECT, VALUE
    }
}
