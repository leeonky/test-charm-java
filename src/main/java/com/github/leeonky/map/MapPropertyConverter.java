package com.github.leeonky.map;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

import java.util.Map;

class MapPropertyConverter extends BaseConverter {
    private final Mapper mapper;
    private final String keyPropertyName;
    private final String propertyName;
    private final String desName;

    MapPropertyConverter(Mapper mapper, String keyPropertyName, String propertyName, String desName) {
        this.mapper = mapper;
        this.keyPropertyName = keyPropertyName;
        this.propertyName = propertyName;
        this.desName = desName;
    }

    @Override
    public String buildConvertId() {
        return String.format("MapPropertyConverter:%s:%s-%s[%d]", keyPropertyName, propertyName, desName, mapper.hashCode());
    }

    @Override
    public Object convert(Object source, Type<?> destinationType, MappingContext mappingContext) {
        Class<?> rawType = destinationType.getRawType();
        Iterable collection = source instanceof Map ? wrapperEntry((Map) source) : (Iterable) source;
        return mapMap(collection, createMap(rawType), ((Type) destinationType.getActualTypeArguments()[1]).getRawType());
    }

    @SuppressWarnings("unchecked")
    private Map mapMap(Iterable source, Map result, Class elementType) {
        source.forEach(e -> result.put(getPropertyValue(e, keyPropertyName), mapper.mapTo(getPropertyValue(e, propertyName), elementType)));
        return result;
    }
}
