package com.github.leeonky.map;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.Converter;
import com.github.leeonky.util.ConverterFactory;
import com.github.leeonky.util.PropertyWriter;
import org.reflections.Reflections;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.leeonky.map.Mapper.guessValueInSequence;

public class PermitMapper {
    private static final Class<?>[] VOID_SCOPES = {void.class};
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private final Converter converter = ConverterFactory.create();
    private final PermitRegisterConfig permitRegisterConfig = new PermitRegisterConfig();
    private Class<?> scope = void.class;

    public PermitMapper(String... packages) {
        Set<Class<?>> classes = new HashSet<>();
        Reflections reflections = new Reflections((Object[]) packages);
        classes.addAll(reflections.getTypesAnnotatedWith(Permit.class));
        classes.addAll(reflections.getTypesAnnotatedWith(PermitTarget.class));
        classes.addAll(reflections.getTypesAnnotatedWith(PermitAction.class));
        classes.forEach(this::register);
    }

    private Class<?>[] getTargetsFromPermitTarget(Class<?> type) {
        PermitTarget permitTarget = type.getDeclaredAnnotation(PermitTarget.class);
        if (permitTarget != null)
            return permitTarget.value();
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getTargetsFromPermit(Class<?> type) {
        Permit permit = type.getDeclaredAnnotation(Permit.class);
        if (permit != null)
            return permit.target();
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getTargetsFromDeclaring(Class<?> type) {
        Class<?> declaringClass = type.getDeclaringClass();
        if (declaringClass != null)
            return getTargets(declaringClass);
        return EMPTY_CLASS_ARRAY;
    }

    private void register(Class<?> type) {
        permitRegisterConfig.register(getActions(type), getTargets(type), getScopes(type, VOID_SCOPES),
                getParents(type, VOID_SCOPES), type);
        PolymorphicPermitIdentityString identityString = type.getAnnotation(PolymorphicPermitIdentityString.class);
        if (identityString != null)
            permitRegisterConfig.registerPolymorphic(getActions(type), getScopes(type, VOID_SCOPES),
                    identityString.value(), type);
    }

    private Class<?>[] getParentsFromPermit(Class<?> type) {
        Permit permit = type.getDeclaredAnnotation(Permit.class);
        if (permit != null)
            return permit.parent();
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getParents(Class<?> type, Class<?>[] defaultReturn) {
        return guessValueInSequence(type, defaultReturn,
                this::getParentsFromPermit);
    }

    private Class<?>[] getTargets(Class<?> type) {
        return guessValueInSequence(type, EMPTY_CLASS_ARRAY,
                this::getTargetsFromPermitTarget,
                this::getTargetsFromPermit,
                this::getTargetsFromDeclaring,
                this::getTargetsFromSuper
        );
    }

    private Class<?>[] getTargetsFromSuper(Class<?> type) {
        Class<?> superclass = type.getSuperclass();
        if (superclass != null)
            return getTargets(superclass);
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getActions(Class<?> type) {
        PermitAction permitAction = type.getDeclaredAnnotation(PermitAction.class);
        if (permitAction != null)
            return new Class<?>[]{permitAction.value()};
        Permit annotation = type.getAnnotation(Permit.class);
        if (annotation != null)
            return annotation.action();
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getScopesFromPermitScope(Class<?> type) {
        PermitScope permitScope = type.getAnnotation(PermitScope.class);
        if (permitScope != null)
            return permitScope.value();
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getScopesFromPermit(Class<?> type) {
        Permit permit = type.getAnnotation(Permit.class);
        if (permit != null)
            return permit.scope();
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getScopes(Class<?> type, Class<?>[] defaultReturn) {
        return guessValueInSequence(type, defaultReturn,
                this::getScopesFromPermitScope,
                this::getScopesFromPermit,
                this::getScopesFromDeclaring,
                this::getScopesFromSuper
        );
    }

    private Class<?>[] getScopesFromSuper(Class<?> type) {
        Class<?> superclass = type.getSuperclass();
        if (superclass != null)
            return getScopes(superclass, EMPTY_CLASS_ARRAY);
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getScopesFromDeclaring(Class<?> type) {
        Class<?> declaringClass = type.getDeclaringClass();
        if (declaringClass != null)
            return getScopes(declaringClass, EMPTY_CLASS_ARRAY);
        return EMPTY_CLASS_ARRAY;
    }

    public <T> T permit(T object, Class<?> target, Class<?> action) {
        return permit(object, target, action, void.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T permit(T object, Class<?> target, Class<?> action, Class<?> parent) {
        return (T) findPermit(target, action, parent).map(p -> {
            if (object instanceof Map)
                return permitMap((Map<String, ?>) object, p);
            else if (object instanceof List)
                return permitList((List<?>) object, p);
            else
                throw new IllegalArgumentException("Not support type " + object.getClass().getName()
                        + ", only support Map or List<Map>");
        }).orElse(object);
    }

    @SuppressWarnings("unchecked")
    private List<?> permitList(List<?> list, Class<?> permit) {
        return list.stream().map(m -> permitMap((Map<String, ?>) m, permit)).collect(Collectors.toList());
    }

    private Map<String, ?> permitMap(Map<String, ?> map, Class<?> permit) {
        return collectPermittedProperties(map, permit)
                .reduce(new LinkedHashMap<>(), (result, property) -> assignToResult(result, property,
                                permitPropertyObjectValue(property.getType(), map.get(property.getName()), permit, property)),
                        Mapper::NotSupportParallelStreamReduce);
    }

    private Stream<? extends PropertyWriter<?>> collectPermittedProperties(Map<String, ?> map, Class<?> permit) {
        return BeanClass.create(permit).getPropertyWriters().values().stream()
                .filter(property -> map.containsKey(property.getName()));
    }

    private LinkedHashMap<String, Object> assignToResult(LinkedHashMap<String, Object> result,
                                                         PropertyWriter<?> property, Object value) {
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
    private LinkedHashMap<String, Object> assignToNestedMap(LinkedHashMap<String, Object> result, Object value,
                                                            String[] propertyChain) {
        LinkedHashMap<String, Object> reduce = Arrays.stream(propertyChain, 0, propertyChain.length - 1)
                .reduce(result, (m, p) -> (LinkedHashMap<String, Object>) m.computeIfAbsent(p, k -> new LinkedHashMap<>()),
                        Mapper::NotSupportParallelStreamReduce);
        reduce.put(propertyChain[propertyChain.length - 1], value);
        return reduce;
    }

    public Optional<Class<?>> findPermit(Class<?> target, Class<?> action, Class<?> parent) {
        return permitRegisterConfig.findPermit(target, action, scope, parent);
    }

    public void setScope(Class<?> scope) {
        this.scope = scope;
    }

    @SuppressWarnings("unchecked")
    private Object permitPropertyObjectValue(BeanClass<?> type, Object value, Class<?> containingPermit,
                                             PropertyWriter<?> property) {
        Class<?> permit = type.getType();
        if (value instanceof Map) {
            return processPolymorphicAndPermitMap((Map<String, ?>) value, permit, containingPermit, property);
        } else if (value instanceof Iterable) {
            return processPolymorphicAndPermitList(type, (Iterable<?>) value, containingPermit, property);
        } else {
            Transform transform = property.getAnnotation(Transform.class);
            if (transform != null)
                value = BeanClass.newInstance(transform.value()).transform(value);
            return converter.tryConvert(permit, value);
        }
    }

    private Object processPolymorphicAndPermitList(BeanClass<?> type, Iterable<?> value, Class<?> containingPermit,
                                                   PropertyWriter<?> property) {
        BeanClass<?> subGenericType = type.getTypeArguments(0)
                .orElseThrow(() -> new IllegalStateException(String.format("Should specify element type in '%s::%s'",
                        containingPermit.getName(), property.getName())));
        return StreamSupport.stream(value.spliterator(), false)
                .map(e -> permitPropertyObjectValue(subGenericType, e, containingPermit, property))
                .collect(Collectors.toList());
    }

    private Object processPolymorphicAndPermitMap(Map<String, ?> value, Class<?> permit, Class<?> containingPermit,
                                                  PropertyWriter<?> property) {
        PermitAction action = property.getAnnotation(PermitAction.class);
        if (action != null) {
            PolymorphicPermitIdentity polymorphicPermitIdentity = permit.getAnnotation(PolymorphicPermitIdentity.class);
            if (polymorphicPermitIdentity == null)
                throw new IllegalStateException("Should specify property name via @PolymorphicPermitIdentity in '"
                        + permit.getName() + "'");
            return permitMap(value, permitRegisterConfig.findPolymorphicPermit(permit, action.value(), scope,
                            value.get(polymorphicPermitIdentity.value()))
                    .orElseThrow(() -> new IllegalStateException(String.format("Cannot find permit for %s[%s] in '%s::%s'",
                            polymorphicPermitIdentity.value(), value.get(polymorphicPermitIdentity.value()),
                            containingPermit.getName(), property.getName()))));
        }
        return permitMap(value, permit);
    }
}
