package com.github.leeonky.jfactory.collector;

import com.github.leeonky.dal.runtime.ProxyObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UnitCollector implements ProxyObject {
    private final LinkedHashMap<String, UnitCollector> fields = new LinkedHashMap<>();
    private Object value;

    @Override
    public Object getValue(Object property) {
        return fields.computeIfAbsent((String) property, k -> new UnitCollector());
    }

    public void setValue(Object value) {
        this.value = value;
    }

    private Object value() {
        return value;
    }

    protected Map<String, ?> propertiesMap() {
        return fields.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().value()));
    }
}
