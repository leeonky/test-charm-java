package com.github.leeonky.map;


import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

import java.lang.reflect.Array;
import java.util.*;

import static ma.glasnost.orika.metadata.TypeFactory.valueOf;

public class ViewConverter extends CustomConverter<Object, Object> {
    protected final Class<?> view;
    protected final Mapper mapper;

    public ViewConverter(Mapper mapper, Class<?> view) {
        this.view = view;
        this.mapper = mapper;
    }

    @Override
    public Object convert(Object source, Type destinationType, MappingContext mappingContext) {
        Class<?> rawType = destinationType.getRawType();
        if (Iterable.class.isAssignableFrom(rawType))
            return mapCollection((Iterable) source, createCollection(rawType), mappingContext);
        if (rawType.isArray())
            return mapCollection((Iterable) source, new ArrayList<>(), mappingContext).toArray((Object[]) Array.newInstance(rawType.getComponentType(), 0));
        if (Map.class.isAssignableFrom(rawType))
            return mapMap((Map) source, createMap(rawType), mappingContext);
        return map(source, mappingContext);
    }

    protected Object map(Object source, MappingContext mappingContext) {
        return mapper.findMapping(source, view).map(d -> {
            Object mappedObject = mappingContext.getMappedObject(source, valueOf(d));
            return mappedObject != null ? mappedObject : mapper.mapTo(source, d);
        }).orElse(null);
    }

    @SuppressWarnings("unchecked")
    private Map mapMap(Map<?, ?> source, Map result, MappingContext mappingContext) {
        source.forEach((k, v) -> result.put(k, map(v, mappingContext)));
        return result;
    }

    protected Map createMap(Class<?> rawType) {
        Map map;
        if (rawType.isInterface())
            map = new LinkedHashMap();
        else {
            try {
                map = (Map) rawType.getConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Can not create instance of " + rawType.getName(), e);
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Collection mapCollection(Iterable source, Collection result, MappingContext mappingContext) {
        for (Object e : source)
            result.add(map(e, mappingContext));
        return result;
    }

    protected Collection createCollection(Class<?> rawType) {
        Collection result;
        if (rawType.isInterface()) {
            if (Set.class.isAssignableFrom(rawType))
                result = new LinkedHashSet();
            else
                result = new ArrayList<>();
        } else {
            try {
                result = (Collection) rawType.getConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Can not create instance of " + rawType.getName(), e);
            }
        }
        return result;
    }

    public String buildConvertId() {
        return String.format("ViewConverter:%s[%d]", view.getName(), mapper.hashCode());
    }
}
