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
    private static final ArrayList<Class<?>> EMPTY_LIST = new ArrayList<>();
    private static final HashMap<Class<?>, Map<Class<?>, Class<?>>> EMPTY_MAP1 = new HashMap<>();
    private static final Map<Class<?>, Map<Class<?>, List<Class<?>>>> EMPTY_MAP2 = new HashMap<>();
    private static final Map<Class<?>, List<Class<?>>> EMPTY_MAP3 = new HashMap<>();
    private Map<Class<?>, Map<Class<?>, Map<Class<?>, Class<?>>>> targetActionScopePermits = new HashMap<>();
    private Map<Object, Map<Class<?>, Map<Class<?>, List<Class<?>>>>> typeActionScopeSubPermits = new HashMap<>();
    private Class<?> scope = void.class;

    public PermitMapper(String... packages) {
        new Reflections((Object[]) packages).getTypesAnnotatedWith(Permit.class)
                .forEach(this::register);
    }

    private void register(Class<?> type) {
        Permit permit = type.getAnnotation(Permit.class);
        PermitScope annotation = type.getAnnotation(PermitScope.class);
        Class<?>[] scopes = annotation == null ? permit.scope() : annotation.value();
        if (scopes.length == 0)
            scopes = new Class<?>[]{void.class};

        Map<Class<?>, Class<?>> scopePermits = targetActionScopePermits.computeIfAbsent(permit.target(), k -> new HashMap<>())
                .computeIfAbsent(permit.action(), k -> new HashMap<>());
        for (Class<?> scope : scopes)
            scopePermits.put(scope, type);

        SubPermitPropertyStringValue typeAnnotation = type.getAnnotation(SubPermitPropertyStringValue.class);
        if (typeAnnotation != null) {
            Map<Class<?>, List<Class<?>>> subScopePermits = typeActionScopeSubPermits.computeIfAbsent(typeAnnotation.value(), t -> new HashMap<>())
                    .computeIfAbsent(permit.action(), a -> new HashMap<>());
            for (Class<?> scope : scopes)
                subScopePermits.computeIfAbsent(scope, s -> new ArrayList<>()).add(type);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T permit(T map, Class<?> target, Class<?> action) {
        return (T) findPermit(target, action).map(p -> {
            if (map instanceof Map)
                return permitMap((Map<String, ?>) map, p);
            else if (map instanceof List)
                return permitList((List<?>) map, p);
            else
                throw new IllegalArgumentException("Not support type " + map.getClass().getName());
        }).orElse(map);
    }

    @SuppressWarnings("unchecked")
    private List<?> permitList(List<?> list, Class<?> permit) {
        return list.stream().map(m -> permitMap((Map<String, ?>) m, permit)).collect(Collectors.toList());
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
                Map<Class<?>, List<Class<?>>> scopeSubPermits = typeActionScopeSubPermits.getOrDefault(polymophismValue, EMPTY_MAP2)
                        .getOrDefault(action.value(), EMPTY_MAP3);
                List<Class<?>> subPermits = scopeSubPermits.get(scope);
                Class<?> subType = (subPermits != null ? subPermits : scopeSubPermits.getOrDefault(void.class, EMPTY_LIST)).stream()
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
        Map<Class<?>, Class<?>> scopePermits = targetActionScopePermits.getOrDefault(target, EMPTY_MAP1)
                .getOrDefault(action, new HashMap<>());
        Class<?> permit = scopePermits.get(scope);
        return Optional.ofNullable(permit != null ? permit : scopePermits.get(void.class));
    }

    public void setScope(Class<?> scope) {
        this.scope = scope;
    }
}
