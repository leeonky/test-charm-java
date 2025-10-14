package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class KeyValueCollection {
    private final Map<String, KeyValue> keyValues = new LinkedHashMap<>();

    public void insertAll(KeyValueCollection another) {
        LinkedHashMap<String, KeyValue> merged = new LinkedHashMap<String, KeyValue>() {{
            putAll(another.keyValues);
            putAll(keyValues);
        }};
        keyValues.clear();
        keyValues.putAll(merged);
    }

    public void appendAll(KeyValueCollection another) {
        keyValues.putAll(another.keyValues);
    }

    Builder<?> apply(Builder<?> builder) {
        for (KeyValue keyValue : keyValues.values())
            builder = keyValue.apply(builder);
        return builder;
    }

    //    TODO remove arg type
    <T> Collection<Expression<T>> expressions(BeanClass<T> type, ObjectFactory<T> objectFactory,
                                              Producer<T> producer, boolean forQuery) {
        return keyValues.values().stream().map(keyValue -> keyValue.createExpression(type, objectFactory, producer, forQuery))
                .collect(Collectors.groupingBy(Expression::getProperty)).values().stream()
                .map(Expression::merge)
                .collect(Collectors.toList());
    }

    public KeyValueCollection append(String key, Object value) {
        keyValues.put(key, new KeyValue(key, value));
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(KeyValueCollection.class, keyValues.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        return BeanClass.cast(obj, KeyValueCollection.class)
                .map(keyValueCollection -> Objects.equals(keyValues, keyValueCollection.keyValues))
                .orElseGet(() -> super.equals(obj));
    }

    public <T> Matcher<T> matcher(BeanClass<T> type, ObjectFactory<T> objectFactory, Producer<T> producer) {
        return new Matcher<>(type, objectFactory, producer);
    }

    public class Matcher<T> {
        private final Collection<Expression<T>> expressions;

        Matcher(BeanClass<T> type, ObjectFactory<T> objectFactory, Producer<T> producer) {
            expressions = expressions(type, objectFactory, producer, true);
        }

        public boolean matches(T object) {
            return expressions.stream().allMatch(e -> e.isMatch(object));
        }
    }

    public boolean isEmpty() {
        return keyValues.isEmpty();
    }
}
