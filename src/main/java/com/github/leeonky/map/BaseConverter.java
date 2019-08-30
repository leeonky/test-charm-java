package com.github.leeonky.map;

import com.github.leeonky.util.BeanClass;
import ma.glasnost.orika.CustomConverter;

import java.util.*;
import java.util.stream.Collectors;

abstract class BaseConverter extends CustomConverter<Object, Object> {

    static Map createMap(Class<?> rawType) {
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

    static Collection createCollection(Class<?> rawType) {
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

    @SuppressWarnings("unchecked")
    static Object getPropertyValue(Object e, String propertyChain) {
        for (String property : propertyChain.split("\\."))
            if (!property.isEmpty())
                e = ((BeanClass) BeanClass.create(e.getClass())).getPropertyValue(e, property);
        return e;
    }

    @SuppressWarnings("unchecked")
    static Iterable wrapperEntry(Map map) {
        return (Iterable) map.entrySet().stream()
                .map(e -> new ViewListPropertyConverter.Entry((Map.Entry) e))
                .collect(Collectors.toList());
    }

    public abstract String buildConvertId();
}
