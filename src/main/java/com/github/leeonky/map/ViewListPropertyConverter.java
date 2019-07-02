package com.github.leeonky.map;

import com.github.leeonky.map.util.BeanUtil;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class ViewListPropertyConverter extends ViewConverter {
    protected final String valueProperty;

    public ViewListPropertyConverter(Mapper mapper, Class<?> view, String valueProperty) {
        super(mapper, view);
        this.valueProperty = valueProperty;
    }

    @SuppressWarnings("unchecked")
    public static Iterable wrapperEntry(Map map) {
        return (Iterable) map.entrySet().stream()
                .map(e -> new Entry((Map.Entry) e))
                .collect(Collectors.toList());
    }

    public static Object getPropertyValue(Object e, String propertyChain) {
        for (String property : propertyChain.split("\\.")) {
            try {
                e = BeanUtil.getPropertyValue(e, property);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return e;
    }

    @Override
    public Object convert(Object source, Type destinationType, MappingContext mappingContext) {
        Class<?> rawType = destinationType.getRawType();
        Iterable collection = source instanceof Map ? wrapperEntry((Map) source) : (Iterable) source;
        if (Iterable.class.isAssignableFrom(rawType))
            return mapCollection(collection, newCollection(rawType), mappingContext);
        else if (rawType.isArray())
            return mapCollection(collection, new ArrayList<>(), mappingContext).toArray((Object[]) Array.newInstance(rawType.getComponentType(), 0));
        throw new IllegalStateException("Only support map " + valueProperty + " to list or array, but target type is " + rawType.getName());
    }

    private Collection<Object> mapCollection(Iterable source, Collection<Object> result, MappingContext mappingContext) {
        for (Object e : source)
            result.add(map(getPropertyValue(e, valueProperty), mappingContext));
        return result;
    }

    public static class Entry {
        private Object key, value;

        public Entry(Map.Entry e) {
            key = e.getKey();
            value = e.getValue();
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }
}
