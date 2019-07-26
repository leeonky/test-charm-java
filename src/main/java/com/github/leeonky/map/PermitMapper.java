package com.github.leeonky.map;

import com.github.leeonky.map.schemas.Permit;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;
import org.reflections.Reflections;

import java.util.*;

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
            if (map.containsKey(key)) {
                Object value = map.get(key);
                if (value instanceof Map)
                    result.put(key, permit((Map<String, ?>) value, propertyWriter.getPropertyType()));
                else if (value instanceof Iterable) {
                    ArrayList<Object> list = new ArrayList<>();
                    result.put(key, list);
                    Class<?> type = propertyWriter.getGenericType().getGenericTypeParameter(0).get().getRawType();
                    for (Object e : (Iterable) value) {
                        list.add(permit((Map<String, ?>) e, type));
                    }
                } else
                    result.put(key, propertyWriter.tryConvert(value));
            }
        });
        return result;
    }

    public Optional<Class<?>> findPermit(Class<?> target, Class<?> action) {
        return Optional.ofNullable(targetActionPermits.getOrDefault(target, EMPTY_MAP).get(action));
    }
}
