package org.testcharm.map;

import ma.glasnost.orika.CustomConverter;
import org.testcharm.util.BeanClass;
import org.testcharm.util.Classes;

import java.util.*;
import java.util.stream.Collectors;

import static org.testcharm.util.Classes.newInstance;

abstract class BaseConverter extends CustomConverter<Object, Object> {

    static Map createMap(Class<?> rawType) {
        return rawType.isInterface() ? new LinkedHashMap() : (Map) newInstance(rawType);
    }

    static Collection createCollection(Class<?> rawType) {
        Collection result;
        if (rawType.isInterface()) {
            result = Set.class.isAssignableFrom(rawType) ? new LinkedHashSet() : new ArrayList<>();
        } else
            result = (Collection) newInstance(rawType);
        return result;
    }

    @SuppressWarnings("unchecked")
    static Object getPropertyValue(Object e, String propertyChain) {
        for (String property : propertyChain.split("\\."))
            if (!property.isEmpty())
                e = ((BeanClass) BeanClass.createFrom(e)).getPropertyValue(e, property);
        return e;
    }

    @SuppressWarnings("unchecked")
    static Iterable wrapperEntry(Map map) {
        return (Iterable) map.entrySet().stream()
                .map(e -> new ViewListConverter.Entry((Map.Entry) e))
                .collect(Collectors.toList());
    }

    public abstract String buildConvertId();

    @Override
    public int hashCode() {
        return buildConvertId().hashCode();
    }

    @SuppressWarnings("EqualsDoesntCheckParameterClass")
    @Override
    public boolean equals(Object other) {
        return Classes.equals(this, other, BaseConverter.class, (self, another) ->
                self.buildConvertId().equals(another.buildConvertId()));
    }
}
