package com.github.leeonky.map;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Stream.of;

class PermitRegisterConfig {
    private Map<Class<?>, Map<Class<?>, Map<Class<?>, Class<?>>>> targetActionScopePermits = new HashMap<>();
    private Map<Object, Map<Class<?>, Map<Class<?>, List<Class<?>>>>> typeActionScopePolymorphicPermits = new HashMap<>();

    void registerPolymorphic(Class<?>[] actions, Class<?>[] scopes, String polymorphicIdentityValue, Class<?> permit) {
        for (Class<?> action : actions) {
            Map<Class<?>, List<Class<?>>> subScopePermits = typeActionScopePolymorphicPermits.computeIfAbsent(
                    polymorphicIdentityValue, t -> new HashMap<>())
                    .computeIfAbsent(action, a -> new HashMap<>());
            for (Class<?> scope : scopes)
                subScopePermits.computeIfAbsent(scope, s -> new ArrayList<>()).add(permit);
        }
    }

    void register(Class<?>[] actions, Class<?>[] targets, Class<?>[] scopes, Class<?> permit) {
        for (Class<?> action : actions)
            for (Class<?> target : targets)
                targetActionScopePermits.computeIfAbsent(target, k -> new HashMap<>())
                        .computeIfAbsent(action, k -> new HashMap<>())
                        .putAll(of(scopes).collect(Collectors.toMap(s -> s, s -> permit)));
    }

    Optional<Class<?>> findPermit(Class<?> target, Class<?> action, Class<?> scope) {
        Map<Class<?>, Class<?>> scopePermits = targetActionScopePermits.getOrDefault(target, emptyMap())
                .getOrDefault(action, new HashMap<>());
        Class<?> permit = scopePermits.get(scope);
        return Optional.ofNullable(permit != null ? permit : scopePermits.get(void.class));
    }

    Optional<Class<?>> findPolymorphicPermit(Class<?> supperPermit, Class<?> action, Class<?> scope, Object polymorphicIdentityValue) {
        Map<Class<?>, List<Class<?>>> scopeSubPermits = typeActionScopePolymorphicPermits.getOrDefault(polymorphicIdentityValue, emptyMap())
                .getOrDefault(action, emptyMap());
        List<Class<?>> polymorphicPermits = scopeSubPermits.get(scope);
        return (polymorphicPermits != null ? polymorphicPermits : scopeSubPermits.getOrDefault(void.class, emptyList())).stream()
                .filter(supperPermit::isAssignableFrom)
                .findFirst();
    }
}
