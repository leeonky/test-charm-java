package com.github.leeonky.map;

import com.github.leeonky.map.schemas.Permit;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.Converter;
import com.github.leeonky.util.GenericType;
import com.github.leeonky.util.PropertyWriter;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PermitMapper {
    private static final HashMap<Class<?>, Class<?>> EMPTY_MAP = new HashMap<>();
    private Map<Class<?>, Map<Class<?>, Class<?>>> targetActionPermits = new HashMap<>();

    public PermitMapper(String... packages) {
        new Reflections((Object[]) packages).getTypesAnnotatedWith(Permit.class)
                .forEach(this::register);
    }

    private void register(Class<?> type) {
        Permit permit = type.getAnnotation(Permit.class);
        targetActionPermits.computeIfAbsent(permit.target(), k -> new HashMap<>())
                .put(permit.action(), type);
    }

    public Map<String, ?> permit(Map<String, ?> map, Class<?> target, Class<?> action) {
        return findPermit(target, action).map(p -> permit(map, p)).get();
    }

    @SuppressWarnings("unchecked")
    public Map<String, ?> permit(Map<String, ?> map, Class<?> permit) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        BeanClass beanClass = BeanClass.createBeanClass(permit);
        ((Map<String, PropertyWriter>) beanClass.getPropertyWriters()).forEach((key, propertyWriter) -> {
            if (map.containsKey(key))
                result.put(key, permitValue(map.get(key), beanClass.getConverter(), propertyWriter.getGenericType()));
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object permitValue(Object value, Converter converter, GenericType genericType) {
        if (value instanceof Map)
            return permit((Map<String, ?>) value, genericType.getRawType());
        else if (value instanceof Iterable) {
            GenericType subType = genericType.getGenericTypeParameter(0).get();
            return StreamSupport.stream(((Iterable) value).spliterator(), false)
                    .map(e -> permitValue(e, converter, subType))
                    .collect(Collectors.toList());
        } else
            return converter.tryConvert(genericType.getRawType(), value);
    }

    public Optional<Class<?>> findPermit(Class<?> target, Class<?> action) {
        return Optional.ofNullable(targetActionPermits.getOrDefault(target, EMPTY_MAP).get(action));
    }
}
