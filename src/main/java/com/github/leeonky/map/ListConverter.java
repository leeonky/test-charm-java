package com.github.leeonky.map;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

class ListConverter extends BaseConverter {
    private final Mapper mapper;
    private final String elementName;
    private final String desName;

    ListConverter(Mapper mapper, String elementName, String desName) {
        this.mapper = mapper;
        this.elementName = elementName;
        this.desName = desName;
    }

    @Override
    public String buildConvertId() {
        return String.format("ListConverter:%s-%s[%d]", elementName, desName, mapper.hashCode());
    }

    @Override
    public Object convert(Object source, Type<?> destinationType, MappingContext mappingContext) {
        Class<?> rawType = destinationType.getRawType();
        Iterable collection = source instanceof Map ? wrapperEntry((Map) source) : (Iterable) source;
        if (Iterable.class.isAssignableFrom(rawType))
            return mapCollection(collection, createCollection(rawType), ((Type) destinationType.getActualTypeArguments()[0]).getRawType());
        if (rawType.isArray())
            return mapCollection(collection, new ArrayList<>(), rawType.getComponentType()).toArray((Object[]) Array.newInstance(rawType.getComponentType(), 0));
        throw new IllegalStateException(String.format("Type of '%s.%s' is invalid, expect Iterable or Array",
                mappingContext.getResolvedDestinationType().getName(), desName));
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> mapCollection(Iterable source, Collection result, Class elementType) {
        for (Object object : source)
            result.add(mapper.mapTo(getPropertyValue(object, elementName), elementType));
        return result;
    }
}
