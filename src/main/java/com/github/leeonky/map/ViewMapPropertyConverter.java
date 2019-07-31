package com.github.leeonky.map;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

import java.util.Map;

public class ViewMapPropertyConverter extends ViewListPropertyConverter {
    private final String keyPropertyName;

    public ViewMapPropertyConverter(Mapper mapper, Class<?> view, String keyPropertyName, String propertyName) {
        super(mapper, view, propertyName);
        this.keyPropertyName = keyPropertyName;
    }

    @Override
    public Object convert(Object source, Type destinationType, MappingContext mappingContext) {
        Class<?> rawType = destinationType.getRawType();
        Iterable collection = source instanceof Map ? wrapperEntry((Map) source) : (Iterable) source;
        return mapMap(collection, newMap(rawType), mappingContext);
    }

    @SuppressWarnings("unchecked")
    private Map mapMap(Iterable source, Map result, MappingContext mappingContext) {
        source.forEach(e -> result.put(getPropertyValue(e, keyPropertyName), map(getPropertyValue(e, propertyName), mappingContext)));
        return result;
    }

    @Override
    public String buildConvertId() {
        return String.format("ViewMapPropertyConverter:%s:%s:%s[%d]", keyPropertyName, propertyName, view.getName(), mapper.hashCode());
    }
}
