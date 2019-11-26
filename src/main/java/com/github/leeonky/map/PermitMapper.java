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

public class PermitMapper {
    private static final Class<?>[] VOID_SCOPES = {void.class};
    private Class<?> scope = void.class;
    private Converter converter = Converter.createDefault();
    private PermitRegisterConfig permitRegisterConfig = new PermitRegisterConfig();

    public PermitMapper(String... packages) {
        new Reflections((Object[]) packages)
                .getTypesAnnotatedWith(Permit.class)
                .forEach(this::register);
    }

    private void register(Class<?> type) {
        permitRegisterConfig.register(type.getAnnotation(Permit.class).action(),
                type.getAnnotation(Permit.class).target(), getScopes(type), type);
        PolymorphicPermitIdentityString identityString = type.getAnnotation(PolymorphicPermitIdentityString.class);
        if (identityString != null)
            permitRegisterConfig.registerPolymorphic(type.getAnnotation(Permit.class).action(),
                    getScopes(type), identityString.value(), type);
    }

    private Class<?>[] getScopes(Class<?> type) {
        Class<?>[] scopes = type.getAnnotation(PermitScope.class) == null ?
                type.getAnnotation(Permit.class).scope()
                : type.getAnnotation(PermitScope.class).value();
        return scopes.length == 0 ? VOID_SCOPES : scopes;
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
        return collectPermittedProperties(map, permit)
                .reduce(new LinkedHashMap<>(), (result, property) -> assignToResult(result, property,
                        permitPropertyObjectValue(map.get(property.getName()), property.getGenericType(), permit, property)),
                        Mapper::NotSupportParallelStreamReduce);
    }

    private Stream<? extends PropertyWriter<?>> collectPermittedProperties(Map<String, ?> map, Class<?> permit) {
        return BeanClass.create(permit).getPropertyWriters().values().stream()
                .filter(property -> map.containsKey(property.getName()));
    }

    private LinkedHashMap<String, Object> assignToResult(LinkedHashMap<String, Object> result, PropertyWriter<?> property, Object value) {
        ToProperty toProperty = property.getAnnotation(ToProperty.class);
        if (toProperty != null) {
            String propertyChain = toProperty.value();
            if (propertyChain.contains("{")) {
                String[] chains = propertyChain.replace("}", "").split("\\{", 2);
                List<LinkedHashMap<String, Object>> listValue = ((Collection<Object>) value).stream()
                        .map(o -> assignToNestedMap(new LinkedHashMap<>(), o, chains[1].split("\\.")))
                        .collect(Collectors.toList());
                if (chains[0].isEmpty())
                    result.put(property.getName(), listValue);
                else
                    assignToNestedMap(result, listValue, chains[0].split("\\."));
            } else
                assignToNestedMap(result, value, propertyChain.split("\\."));
        } else
            result.put(property.getName(), value);
        return result;
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, Object> assignToNestedMap(LinkedHashMap<String, Object> result, Object value, String[] propertyChain) {
        LinkedHashMap<String, Object> reduce = Arrays.stream(propertyChain, 0, propertyChain.length - 1)
                .reduce(result, (m, p) -> (LinkedHashMap<String, Object>) m.computeIfAbsent(p, k -> new LinkedHashMap<>()),
                        Mapper::NotSupportParallelStreamReduce);
        reduce.put(propertyChain[propertyChain.length - 1], value);
        return reduce;
    }

    public Optional<Class<?>> findPermit(Class<?> target, Class<?> action) {
        return permitRegisterConfig.findPermit(target, action, scope);
    }

    public void setScope(Class<?> scope) {
        this.scope = scope;
    }

    @SuppressWarnings("unchecked")
    private Object permitPropertyObjectValue(Object value, GenericType genericType, Class<?> containingPermit, PropertyWriter<?> property) {
        Class<?> permit = genericType.getRawType();
        if (value instanceof Map) {
            return processPolymorphicAndPermitMap((Map<String, ?>) value, permit, containingPermit, property);
        } else if (value instanceof Iterable) {
            return processPolymorphicAndPermitList((Iterable<?>) value, genericType, containingPermit, property);
        } else
            return converter.tryConvert(permit, value);
    }

    private Object processPolymorphicAndPermitList(Iterable<?> value, GenericType genericType, Class<?> containingPermit, PropertyWriter<?> property) {
        GenericType subGenericType = genericType.getGenericTypeParameter(0)
                .orElseThrow(() -> new IllegalStateException(String.format("Should specify element type in '%s::%s'", containingPermit.getName(), property.getName())));
        return StreamSupport.stream(value.spliterator(), false)
                .map(e -> permitPropertyObjectValue(e, subGenericType, containingPermit, property))
                .collect(Collectors.toList());
    }

    private Object processPolymorphicAndPermitMap(Map<String, ?> value, Class<?> permit, Class<?> containingPermit, PropertyWriter<?> property) {
        PermitAction action = property.getAnnotation(PermitAction.class);
        if (action != null) {
            PolymorphicPermitIdentity polymorphicPermitIdentity = permit.getAnnotation(PolymorphicPermitIdentity.class);
            if (polymorphicPermitIdentity == null)
                throw new IllegalStateException("Should specify property name via @PolymorphicPermitIdentity in '" + permit.getName() + "'");
            return permitMap(value, permitRegisterConfig.findPolymorphicPermit(permit, action.value(), scope, value.get(polymorphicPermitIdentity.value()))
                    .orElseThrow(() -> new IllegalStateException(String.format("Cannot find permit for %s[%s] in '%s::%s'",
                            polymorphicPermitIdentity.value(), value.get(polymorphicPermitIdentity.value()), containingPermit.getName(), property.getName()))));
        }
        return permitMap(value, permit);
    }
}
