package com.github.leeonky.map;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.Converter;
import com.github.leeonky.util.GenericType;
import com.github.leeonky.util.PropertyWriter;
import org.reflections.Reflections;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PermitMapper {
    private static final HashMap<Class<?>, Class<?>> EMPTY_MAP = new HashMap<>();
    private static final HashMap<Class<?>, List<Class<?>>> EMPTY_MAP2 = new HashMap<>();
    private static final List<Class<?>> EMPTY_LIST = new ArrayList<>();
    private Map<Class<?>, Map<Class<?>, Class<?>>> targetActionPermits = new HashMap<>();
    private Map<Object, Map<Class<?>, List<Class<?>>>> typeActionSubPermits = new HashMap<>();

    public PermitMapper(String... packages) {
        new Reflections((Object[]) packages).getTypesAnnotatedWith(Permit.class)
                .forEach(this::register);
    }

    private void register(Class<?> type) {
        Permit permit = type.getAnnotation(Permit.class);
        targetActionPermits.computeIfAbsent(permit.target(), k -> new HashMap<>())
                .put(permit.action(), type);

        SubPermitPropertyStringValue typeAnnotation = type.getAnnotation(SubPermitPropertyStringValue.class);
        if (typeAnnotation != null)
            typeActionSubPermits.computeIfAbsent(typeAnnotation.value(), t -> new HashMap<>())
                    .computeIfAbsent(permit.action(), a -> new ArrayList<>())
                    .add(type);
    }

    public Map<String, ?> permit(Map<String, ?> map, Class<?> target, Class<?> action) {
        //TODO error handler
        return findPermit(target, action).map(p -> permit(map, p)).get();
    }

    @SuppressWarnings("unchecked")
    public Map<String, ?> permit(Map<String, ?> map, Class<?> permit) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        BeanClass beanClass = BeanClass.createBeanClass(permit);
        ((Map<String, PropertyWriter<?>>) beanClass.getPropertyWriters()).forEach((key, propertyWriter) -> {
            if (map.containsKey(key))
                result.put(key, permitValue(map.get(key), beanClass.getConverter(), propertyWriter.getGenericType(),
                        propertyWriter.getAnnotation(PermitAction.class)));
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object permitValue(Object value, Converter converter, GenericType genericType, PermitAction action) {
        Class<?> rawType = genericType.getRawType();
        if (value instanceof Map) {
            if (action != null) {
                //TODO error handler(may null)
                SubPermitProperty annotation = rawType.getAnnotation(SubPermitProperty.class);

                Class<?> subType = typeActionSubPermits.getOrDefault(((Map) value).get(annotation.value()), EMPTY_MAP2)
                        .getOrDefault(action.value(), EMPTY_LIST).stream()
                        .filter(c -> rawType.isAssignableFrom(c))
                        .findFirst().get(); //TODO error handler may empty

                return permit((Map<String, ?>) value, subType);
            }
            return permit((Map<String, ?>) value, rawType);
        } else if (value instanceof Iterable) {
            //TODO error handler may empty
            GenericType subType = genericType.getGenericTypeParameter(0).get();
            return StreamSupport.stream(((Iterable) value).spliterator(), false)
                    .map(e -> permitValue(e, converter, subType, action))
                    .collect(Collectors.toList());
        } else
            return converter.tryConvert(rawType, value);
    }

    public Optional<Class<?>> findPermit(Class<?> target, Class<?> action) {
        return Optional.ofNullable(targetActionPermits.getOrDefault(target, EMPTY_MAP).get(action));
    }
}
