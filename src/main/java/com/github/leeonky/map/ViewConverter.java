package com.github.leeonky.map;


import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

import java.lang.reflect.Array;
import java.util.*;

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
            return mapCollection((Iterable) source, newCollection(rawType));
        if (rawType.isArray())
            return mapCollection((Iterable) source, new ArrayList<>()).toArray((Object[]) Array.newInstance(rawType.getComponentType(), 0));
        if (Map.class.isAssignableFrom(rawType))
            return mapMap((Map) source, newMap(rawType));
        return mapper.map(source, view);
    }

    @SuppressWarnings("unchecked")
    private Map mapMap(Map<?, ?> source, Map result) {
        source.forEach((k, v) -> result.put(k, mapper.map(v, view)));
        return result;
    }

    protected Map newMap(Class<?> rawType) {
        Map map;
        if (rawType.isInterface())
            map = new HashMap();
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
    private Collection mapCollection(Iterable source, Collection result) {
        for (Object e : source)
            result.add(mapper.map(e, view));
        return result;
    }

    protected Collection newCollection(Class<?> rawType) {
        Collection result;
        if (rawType.isInterface()) {
            if (Set.class.isAssignableFrom(rawType))
                result = new HashSet<>();
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
}
