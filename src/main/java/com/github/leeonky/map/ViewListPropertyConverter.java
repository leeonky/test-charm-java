package com.github.leeonky.map;

import com.github.leeonky.map.util.BeanUtil;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

public class ViewListPropertyConverter extends ViewConverter {
    private final String sourceProperty;

    public ViewListPropertyConverter(Mapper mapper, Class<?> view, String sourceProperty) {
        super(mapper, view);
        this.sourceProperty = sourceProperty;
    }

    @Override
    public Object convert(Object source, Type destinationType, MappingContext mappingContext) {
        Class<?> rawType = destinationType.getRawType();
        if (Iterable.class.isAssignableFrom(rawType))
            return mapCollection((Iterable) source, newCollection(rawType));
        else if (rawType.isArray())
            return mapCollection((Iterable) source, new ArrayList<>()).toArray((Object[]) Array.newInstance(rawType.getComponentType(), 0));
        throw new IllegalStateException("Only support map " + sourceProperty + " to list or array, but target type is " + rawType.getName());
    }

    private Collection<Object> mapCollection(Iterable source, Collection<Object> result) {
        for (Object e : source) {
            for (String property : sourceProperty.split("\\.")) {
                try {
                    e = BeanUtil.getPropertyValue(e, property);
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
            result.add(mapper.map(e, view));
        }
        return result;
    }
}
