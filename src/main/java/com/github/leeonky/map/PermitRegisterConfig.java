package com.github.leeonky.map;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

class PermitRegisterConfig {
    private Map<Class<?>, Map<Class<?>, Map<Class<?>, Map<Class<?>, Class<?>>>>> targetActionScopePermits = new HashMap<>();
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

    void register(Class<?>[] actions, Class<?>[] targets, Class<?>[] scopes, Class<?>[] parents, Class<?> permit) {
        for (Class<?> action : actions)
            for (Class<?> parent : parents)
                for (Class<?> target : targets) {
                    Map<Class<?>, Class<?>> scopePermitMap = targetActionScopePermits.computeIfAbsent(parent, k -> new HashMap<>())
                            .computeIfAbsent(target, k -> new HashMap<>())
                            .computeIfAbsent(action, k -> new HashMap<>());
                    for (Class<?> scope : scopes) {
                        Class<?> exist = scopePermitMap.put(scope, permit);
                        if (exist != null && exist != permit)
                            System.err.println(String.format("Warning: %s and %s have the same view and scope in permit mapper ",
                                    exist.getName(), permit.getName()));
                    }
                }
    }

    Optional<Class<?>> findPermit(Class<?> target, Class<?> action, Class<?> scope, Class<?> parent) {
        Map<Class<?>, Class<?>> scopePermits = targetActionScopePermits
                .getOrDefault(parent, emptyMap())
                .getOrDefault(target, emptyMap())
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
