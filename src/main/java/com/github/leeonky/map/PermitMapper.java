package com.github.leeonky.map;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.Converter;
import com.github.leeonky.util.GenericType;
import com.github.leeonky.util.PropertyWriter;
import org.reflections.Reflections;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class PermitMapper {
    private Map<Class<?>, Map<Class<?>, Map<Class<?>, Class<?>>>> targetActionScopePermits = new HashMap<>();
    private Map<Object, Map<Class<?>, Map<Class<?>, List<Class<?>>>>> typeActionScopePolymorphicPermits = new HashMap<>();
    private Class<?> scope = void.class;
    private Converter converter = Converter.createDefault();

    public PermitMapper(String... packages) {
        new Reflections((Object[]) packages).getTypesAnnotatedWith(Permit.class)
                .forEach(type -> {
                    registerPermit(type);
                    registerPolymorphicTypePermit(type);
                });
    }

    private void registerPolymorphicTypePermit(Class<?> type) {
        for (Class<?> action : type.getAnnotation(Permit.class).action())
            if (type.getAnnotation(SubPermitPropertyStringValue.class) != null) {
                Map<Class<?>, List<Class<?>>> subScopePermits = typeActionScopePolymorphicPermits.computeIfAbsent(
                        type.getAnnotation(SubPermitPropertyStringValue.class).value(), t -> new HashMap<>())
                        .computeIfAbsent(action, a -> new HashMap<>());
                for (Class<?> scope : getScopes(type))
                    subScopePermits.computeIfAbsent(scope, s -> new ArrayList<>()).add(type);
            }
    }

    private void registerPermit(Class<?> type) {
        for (Class<?> action : type.getAnnotation(Permit.class).action())
            for (Class<?> target : type.getAnnotation(Permit.class).target())
                targetActionScopePermits.computeIfAbsent(target, k -> new HashMap<>())
                        .computeIfAbsent(action, k -> new HashMap<>())
                        .putAll(getScopes(type).stream().collect(Collectors.toMap(s -> s, s -> type)));
    }

    private List<Class<?>> getScopes(Class<?> type) {
        Class<?>[] scopes = type.getAnnotation(PermitScope.class) == null ?
                type.getAnnotation(Permit.class).scope()
                : type.getAnnotation(PermitScope.class).value();
        if (scopes.length == 0)
            scopes = new Class<?>[]{void.class};
        return asList(scopes);
    }

    @SuppressWarnings("unchecked")
    public <T> T permit(T object, Class<?> target, Class<?> action) {
        return (T) findPermit(target, action).map(p -> {
            if (object instanceof Map)
                return permitMap((Map<String, ?>) object, p);
            else if (object instanceof List)
                return permitList((List<?>) object, p);
            else
                throw new IllegalArgumentException("Not support type " + object.getClass().getName() + ", only support Map or List<Map>");
        }).orElse(object);
    }

    @SuppressWarnings("unchecked")
    private List<?> permitList(List<?> list, Class<?> permit) {
        return list.stream().map(m -> permitMap((Map<String, ?>) m, permit)).collect(Collectors.toList());
    }

    private Map<String, ?> permitMap(Map<String, ?> map, Class<?> permit) {
        return permittedProperties(map, permit)
                .reduce(new LinkedHashMap<>(), (result, property) -> assignToMap(result, property,
                        new PermitActionSpec(property.getAnnotation(PermitAction.class), property.getName(), permit)
                                .permitValue(map.get(property.getName()), property.getGenericType())),
                        Mapper::NotSupportParallelStreamReduce);
    }

    private Stream<? extends PropertyWriter<?>> permittedProperties(Map<String, ?> map, Class<?> permit) {
        return BeanClass.create(permit).getPropertyWriters().values().stream()
                .filter(property -> map.containsKey(property.getName()));
    }

    private LinkedHashMap<String, Object> assignToMap(LinkedHashMap<String, Object> result, PropertyWriter<?> property, Object value) {
        ToProperty toProperty = property.getAnnotation(ToProperty.class);
        if (toProperty != null)
            assignToNestedMap(result, value, toProperty.value().split("\\."));
        else
            result.put(property.getName(), value);
        return result;
    }

    @SuppressWarnings("unchecked")
    private void assignToNestedMap(LinkedHashMap<String, Object> result, Object value, String[] propertyChain) {
        Arrays.stream(propertyChain, 0, propertyChain.length - 1)
                .reduce(result, (m, p) -> (LinkedHashMap<String, Object>) m.computeIfAbsent(p, k -> new LinkedHashMap<>()),
                        Mapper::NotSupportParallelStreamReduce)
                .put(propertyChain[propertyChain.length - 1], value);
    }

    public Optional<Class<?>> findPermit(Class<?> target, Class<?> action) {
        Map<Class<?>, Class<?>> scopePermits = targetActionScopePermits.getOrDefault(target, emptyMap())
                .getOrDefault(action, new HashMap<>());
        Class<?> permit = scopePermits.get(scope);
        return Optional.ofNullable(permit != null ? permit : scopePermits.get(void.class));
    }

    public void setScope(Class<?> scope) {
        this.scope = scope;
    }

    class PermitActionSpec {
        final PermitAction action;
        final String key;
        final Class<?> type;

        PermitActionSpec(PermitAction action, String key, Class<?> type) {
            this.action = action;
            this.key = key;
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        Object permitValue(Object value, GenericType genericType) {
            Class<?> rawType = genericType.getRawType();
            if (value instanceof Map) {
                return processPolymorphicAndPermitMap((Map<String, ?>) value, rawType);
            } else if (value instanceof Iterable) {
                return processPolymorphicAndPermitList((Iterable<?>) value, genericType);
            } else
                return converter.tryConvert(rawType, value);
        }

        private Object processPolymorphicAndPermitList(Iterable<?> value, GenericType genericType) {
            GenericType subType = genericType.getGenericTypeParameter(0)
                    .orElseThrow(() -> new IllegalStateException(String.format("Should specify element type in '%s::%s'", type.getName(), key)));
            return StreamSupport.stream(value.spliterator(), false)
                    .map(e -> permitValue(e, subType))
                    .collect(Collectors.toList());
        }

        private Object processPolymorphicAndPermitMap(Map<String, ?> value, Class<?> rawType) {
            if (action != null) {
                SubPermitProperty annotation = rawType.getAnnotation(SubPermitProperty.class);
                if (annotation == null)
                    throw new IllegalStateException("Should specify property name via @SubPermitProperty in '" + rawType.getName() + "'");

                Map<Class<?>, List<Class<?>>> scopeSubPermits = typeActionScopePolymorphicPermits.getOrDefault(value.get(annotation.value()), emptyMap())
                        .getOrDefault(action.value(), emptyMap());
                List<Class<?>> subPermits = scopeSubPermits.get(scope);
                Class<?> subType = (subPermits != null ? subPermits : scopeSubPermits.getOrDefault(void.class, emptyList())).stream()
                        .filter(rawType::isAssignableFrom)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(String.format("Cannot find permit for %s[%s] in '%s::%s'",
                                annotation.value(), value.get(annotation.value()), type.getName(), key)));
                return permitMap(value, subType);
            }
            return permitMap(value, rawType);
        }
    }
}
