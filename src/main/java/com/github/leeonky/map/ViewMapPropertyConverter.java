package com.github.leeonky.map;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

import java.util.Map;

public class ViewMapPropertyConverter extends ViewListPropertyConverter {
    private final String keyProperty;

    public ViewMapPropertyConverter(Mapper mapper, Class<?> view, String keyProperty, String valueProperty) {
        super(mapper, view, valueProperty);
        this.keyProperty = keyProperty;
    }

    @Override
    public Object convert(Object source, Type destinationType, MappingContext mappingContext) {
        Class<?> rawType = destinationType.getRawType();
        Iterable collection = source instanceof Map ? wrapperEntry((Map) source) : (Iterable) source;
        return mapMap(collection, newMap(rawType));
    }

    @SuppressWarnings("unchecked")
    private Map mapMap(Iterable source, Map result) {
        source.forEach(e -> result.put(getPropertyValue(e, keyProperty), mapper.map(getPropertyValue(e, valueProperty), view)));
        return result;
    }
}
