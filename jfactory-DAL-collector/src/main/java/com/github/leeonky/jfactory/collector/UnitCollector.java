package com.github.leeonky.jfactory.collector;

import com.github.leeonky.dal.runtime.ProxyObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class UnitCollector implements ProxyObject {
    private final LinkedHashMap<String, UnitCollector> fields = new LinkedHashMap<>();
    private Object value;
    private String[] traitsSpec;

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

    protected Map<String, Object> propertiesMap() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        fields.forEach((key, value) -> result.put(key, value.value()));
        return result;
    }

    public UnitCollector setTraitsSpec(String[] traitsSpec) {
        this.traitsSpec = traitsSpec;
        return this;
    }

    public String[] traitsSpec() {
        return traitsSpec;
    }
}
