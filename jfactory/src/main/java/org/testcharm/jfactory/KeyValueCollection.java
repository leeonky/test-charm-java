package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

import java.util.*;
import java.util.stream.Collectors;

public class KeyValueCollection {
    private final List<KeyValue> keyValues = new ArrayList<>();

    public void insertAll(KeyValueCollection another) {
        List<KeyValue> merged = new ArrayList<KeyValue>() {{
            addAll(another.keyValues);
            addAll(keyValues);
        }};
        keyValues.clear();
        keyValues.addAll(merged);
    }

    public void appendAll(KeyValueCollection another) {
        keyValues.addAll(another.keyValues);
    }

    Builder<?> apply(Builder<?> builder) {
        for (KeyValue keyValue : keyValues)
            builder = keyValue.apply(builder);
        return builder;
    }

    //    TODO remove arg type
    <T> Collection<Expression<T>> expressions(BeanClass<T> type, ObjectFactory<T> objectFactory,
                                              Producer<T> producer, boolean forQuery) {
        return keyValues.stream().map(keyValue -> keyValue.createExpression(type, objectFactory, producer, forQuery))
                .collect(Collectors.groupingBy(Expression::getProperty)).values().stream()
                .map(Expression::merge)
                .collect(Collectors.toList());
    }

    public KeyValueCollection append(String key, Object value) {
        keyValues.add(new KeyValue(key, value));
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

    List<SubBuilder> groupByProperty() {
        return keyValues.stream().map(keyValue -> SubBuilder.create(keyValue.key(), keyValue.getValue(), null))
                .collect(Collectors.groupingBy(SubBuilder::property, LinkedHashMap::new, Collectors.toList())).values().stream()
                .map(subBuilders -> subBuilders.stream().reduce(SubBuilder::mergeTo))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
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
