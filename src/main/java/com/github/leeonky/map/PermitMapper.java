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
        return findPermit(target, action).<Map<String, ?>>map(p -> permitMap(map, p)).orElse(map);
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> permitMap(Map<String, ?> map, Class<?> permit) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        BeanClass beanClass = BeanClass.createBeanClass(permit);
        ((Map<String, PropertyWriter<?>>) beanClass.getPropertyWriters()).forEach((key, propertyWriter) -> {
            if (map.containsKey(key))
                result.put(key, permitValue(map.get(key), beanClass.getConverter(), propertyWriter.getGenericType(),
                        propertyWriter.getAnnotation(PermitAction.class), key, permit));
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private Object permitValue(Object value, Converter converter, GenericType genericType, PermitAction action, String key, Class<?> type) {
        Class<?> rawType = genericType.getRawType();
        if (value instanceof Map) {
            if (action != null) {
                SubPermitProperty annotation = rawType.getAnnotation(SubPermitProperty.class);
                if (annotation == null)
                    throw new IllegalStateException("Should specify property name via @SubPermitProperty in '" + rawType.getName() + "'");

                String polymorphismPropertyName = annotation.value();
                Object polymophismValue = ((Map) value).get(polymorphismPropertyName);
                Class<?> subType = typeActionSubPermits.getOrDefault(polymophismValue, EMPTY_MAP2)
                        .getOrDefault(action.value(), EMPTY_LIST).stream()
                        .filter(rawType::isAssignableFrom)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(String.format("Cannot find permit for %s[%s] in '%s::%s'",
                                polymorphismPropertyName, polymophismValue, type.getName(), key)));

                return permitMap((Map<String, ?>) value, subType);
            }
            return permitMap((Map<String, ?>) value, rawType);
        } else if (value instanceof Iterable) {
            GenericType subType = genericType.getGenericTypeParameter(0)
                    .orElseThrow(() -> new IllegalStateException(String.format("Should specify element type in '%s::%s'", type.getName(), key)));
            return StreamSupport.stream(((Iterable) value).spliterator(), false)
                    .map(e -> permitValue(e, converter, subType, action, key, type))
                    .collect(Collectors.toList());
        } else
            return converter.tryConvert(rawType, value);
    }

    public Optional<Class<?>> findPermit(Class<?> target, Class<?> action) {
        return Optional.ofNullable(targetActionPermits.getOrDefault(target, EMPTY_MAP).get(action));
    }
}
