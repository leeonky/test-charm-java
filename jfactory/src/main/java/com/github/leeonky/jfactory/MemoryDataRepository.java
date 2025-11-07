package com.github.leeonky.jfactory;

import java.util.*;

public class MemoryDataRepository implements DataRepository {
    private final Map<Class<?>, Set<Object>> data = new HashMap<>();

    @Override
    public void save(Object object) {
        if (object != null)
            data.computeIfAbsent(object.getClass(), c -> new LinkedHashSet<>()).add(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> queryAll(Class<T> type) {
        return (Collection<T>) data.getOrDefault(type, Collections.emptySet());
    }

    @Override
    public void clear() {
        data.clear();
    }

    public Map<Class<?>, Set<Object>> allData() {
        return data;
    }
}
