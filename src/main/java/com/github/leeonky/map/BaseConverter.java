package com.github.leeonky.map;

import com.github.leeonky.util.BeanClass;
import ma.glasnost.orika.CustomConverter;

import java.util.*;
import java.util.stream.Collectors;

abstract class BaseConverter extends CustomConverter<Object, Object> {

    static Map createMap(Class<?> rawType) {
        return rawType.isInterface() ? new LinkedHashMap() : (Map) BeanClass.newInstance(rawType);
    }

    static Collection createCollection(Class<?> rawType) {
        Collection result;
        if (rawType.isInterface()) {
            result = Set.class.isAssignableFrom(rawType) ? new LinkedHashSet() : new ArrayList<>();
        } else
            result = (Collection) BeanClass.newInstance(rawType);
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
