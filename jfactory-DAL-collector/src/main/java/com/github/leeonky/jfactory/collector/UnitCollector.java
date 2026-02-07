package com.github.leeonky.jfactory.collector;

import com.github.leeonky.dal.runtime.ProxyObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UnitCollector implements ProxyObject {
    private final LinkedHashMap<String, UnitCollector> fields = new LinkedHashMap<>();
    private final List<UnitCollector> list = new ArrayList<>();
    private Object value;
    private String[] traitsSpec;

    @Override
    public Object getValue(Object property) {
        return fields.computeIfAbsent((String) property, k -> this.newElement());
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object value() {
        if (!fields.isEmpty())
            return propertiesMap();
        if (!list.isEmpty())
            return list.stream().map(UnitCollector::value).collect(Collectors.toList());
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

    public UnitCollector newElement() {
        return new UnitCollector();
    }

    public UnitCollector getByIndex(int index) {
        int count = index + 1 - list.size();
        while (count-- > 0)
            list.add(null);
        if (list.get(index) == null)
            list.set(index, newElement());
        return list.get(index);
    }
}
