package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

class QueryExpression<T> {
    private static final int GROUP_PROPERTY = 1;
    private static final int GROUP_COLLECTION_INDEX = 3;
    private static final int GROUP_MIX_IN = 5;
    private static final int GROUP_DEFINITION = 6;
    private static final int GROUP_INTENTLY = 7;
    private static final int GROUP_CONDITION = 9;
    private final BeanClass<T> beanClass;
    private final String property;
    private final ConditionValue<T> conditionValue;

    public QueryExpression(BeanClass<T> beanClass, String chain, Object value) {
        this.beanClass = beanClass;
        Matcher matcher = parse(chain);
        property = matcher.group(GROUP_PROPERTY);
        conditionValue = buildConditionValue(value, matcher, beanClass);
    }

    public QueryExpression(BeanClass<T> beanClass, String property, ConditionValue<T> conditionValue) {
        this.beanClass = beanClass;
        this.property = property;
        this.conditionValue = conditionValue;
    }

    public static <T> Map<String, QueryExpression<T>> createQueryExpressions(BeanClass<T> beanClass, Map<String, Object> criteria) {
        return criteria.entrySet().stream()
                .map(e -> new QueryExpression<>(beanClass, e.getKey(), e.getValue()))
                .collect(Collectors.groupingBy(expression -> expression.property)).values().stream()
                .map(QueryExpression::mergeToSingle)
                .collect(Collectors.toMap(q -> q.property, q -> q));
    }

    private static <T> QueryExpression<T> mergeToSingle(List<QueryExpression<T>> expressions) {
        return expressions.stream().reduce((q1, q2) -> new QueryExpression<>(q1.beanClass, q1.property, q1.conditionValue.merge(q2.conditionValue))).get();
    }

    private ConditionValue<T> buildConditionValue(Object value, Matcher matcher, BeanClass<T> beanClass) {
        String property = matcher.group(GROUP_PROPERTY);
        ConditionValue<T> conditionValue = createConditionValue(value,
                matcher.group(GROUP_MIX_IN) != null ? matcher.group(GROUP_MIX_IN).split(", |,| ") : new String[0],
                matcher.group(GROUP_DEFINITION), matcher.group(GROUP_CONDITION), property, beanClass)
                .setIntently(matcher.group(GROUP_INTENTLY) != null);

        if (matcher.group(GROUP_COLLECTION_INDEX) != null)
            conditionValue = new CollectionConditionValue<>(Integer.valueOf(matcher.group(GROUP_COLLECTION_INDEX)), conditionValue, property, beanClass);
        return conditionValue;
    }

    private Matcher parse(String chain) {
        Matcher matcher = Pattern.compile("([^.(!\\[]+)(\\[(\\d+)])?(\\(([^, ]*[, ])*(.+)\\))?(!)?(\\.(.+))?").matcher(chain);
        if (!matcher.matches()) {
            //TODO not matched should throw exception
        }
        return matcher;
    }

    private ConditionValue<T> createConditionValue(Object value, String[] mixIn, String definition, String condition, String property, BeanClass<T> beanClass) {
        return condition != null ?
                new ConditionValueSet<>(condition, value, mixIn, definition, property, beanClass)
                : new SingleValue<>(value, mixIn, definition, property, beanClass);
    }

    @SuppressWarnings("unchecked")
    public boolean matches(Object object) {
        if (object == null)
            return false;
        PropertyReader propertyReader = beanClass.getPropertyReader(property);
        return conditionValue.matches(propertyReader.getType().getElementOrPropertyType(), propertyReader.getValue(object));
    }

    public Producer<?> buildProducer(FactorySet factorySet, Producer<T> parent, Instance<T> instance) {
        return conditionValue.buildProducer(factorySet, parent, instance);
    }

    public static abstract class ConditionValue<T> {
        protected final String property;
        protected final BeanClass<T> beanClass;

        private boolean intently = false;

        public ConditionValue(String property, BeanClass<T> beanClass) {
            this.property = property;
            this.beanClass = beanClass;
        }

        public abstract boolean matches(BeanClass<?> type, Object propertyValue);

        public abstract Producer<?> buildProducer(FactorySet factorySet, Producer<T> parent, Instance<T> instance);

        public abstract ConditionValue<T> merge(ConditionValue<T> conditionValue);

        protected ConditionValue<T> mergeTo(SingleValue<T> singleValue) {
            throw new IllegalArgumentException(String.format("Cannot merge different structure %s.%s", beanClass.getName(), property));
        }

        protected ConditionValue<T> mergeTo(ConditionValueSet<T> conditionValueSet) {
            throw new IllegalArgumentException(String.format("Cannot merge different structure %s.%s", beanClass.getName(), property));
        }

        protected ConditionValue<T> mergeTo(CollectionConditionValue<T> collectionConditionValue) {
            throw new IllegalArgumentException(String.format("Cannot merge different structure %s.%s", beanClass.getName(), property));
        }

        public boolean isIntently() {
            return intently;
        }

        public ConditionValue<T> setIntently(boolean intently) {
            this.intently = intently;
            return this;
        }
    }

    private static class SingleValue<T> extends ConditionValue<T> {
        private final Object value;
        private final String[] mixIns;
        private final String definition;

        public SingleValue(Object value, String[] mixIns, String definition, String property, BeanClass<T> beanClass) {
            super(property, beanClass);
            this.value = value;
            this.mixIns = mixIns;
            this.definition = definition;
        }

        @Override
        public boolean matches(BeanClass<?> type, Object propertyValue) {
            return !isIntently() && Objects.equals(propertyValue, beanClass.getConverter().tryConvert(type.getType(), value));
        }

        @Override
        @SuppressWarnings("unchecked")
        public Producer<?> buildProducer(FactorySet factorySet, Producer<T> parent, Instance<T> instance) {
//            if (isIntently())
//                return toBuilder(factorySet, beanClass.getPropertyWriter(property).getElementOrPropertyType()).producer(property);
            return new FixedValueProducer(parent.getType().getPropertyWriter(property).getType(), value);
        }

        @Override
        public ConditionValue<T> merge(ConditionValue<T> conditionValue) {
            return conditionValue.mergeTo(this);
        }

        @Override
        protected ConditionValue<T> mergeTo(SingleValue<T> singleValue) {
            return this;
        }

//        private Builder<?> toBuilder(FactorySet factorySet, Class<?> propertyType) {
//            return (definition != null ?
//                    factorySet.toBuild(definition)
//                    : factorySet.type(propertyType))
//                    .mixIn(mixIns);
//        }
    }

    private static class ConditionValueSet<T> extends ConditionValue<T> {
        private final Map<String, Object> conditionValues = new LinkedHashMap<>();
        private String[] mixIns;
        private String definition;

        public ConditionValueSet(String condition, Object value, String[] mixIns, String definition, String property, BeanClass<T> beanClass) {
            super(property, beanClass);
            this.mixIns = mixIns;
            this.definition = definition;
            conditionValues.put(condition, value);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(BeanClass<?> type, Object propertyValue) {
            return conditionValues.entrySet().stream()
                    .map(conditionValue -> new QueryExpression(type, conditionValue.getKey(), conditionValue.getValue()))
                    .allMatch(queryExpression -> queryExpression.matches(propertyValue));
        }

        @Override
        @SuppressWarnings("unchecked")
        public Producer<?> buildProducer(FactorySet factorySet, Producer<T> parent, Instance<T> instance) {
//            if (isIntently())
//                return toBuilder(factorySet, beanClass.getPropertyWriter(property).getElementOrPropertyType()).producer(property);
            Collection<?> collection = toBuilder(factorySet, beanClass.getPropertyReader(property).getType().getElementOrPropertyType()).queryAll();
            if (collection.isEmpty())
                return toBuilder(factorySet, beanClass.getPropertyWriter(property).getType().getElementOrPropertyType()).createProducer(property);
            else
                return new FixedValueProducer(parent.getType().getPropertyWriter(property).getType(), collection.iterator().next());
        }

        private Builder<?> toBuilder(FactorySet factorySet, BeanClass<?> propertyType) {
//            return (definition != null ?
//                    factorySet.toBuild(definition)
//                    : factorySet.type(propertyType))
//                    .mixIn(mixIns).properties(conditionValues);
            return factorySet.type(propertyType.getType()).properties(conditionValues);
        }

        @Override
        public ConditionValue<T> merge(ConditionValue<T> conditionValue) {
            return conditionValue.mergeTo(this);
        }

        @Override
        protected ConditionValue<T> mergeTo(ConditionValueSet<T> conditionValueSet) {
            conditionValueSet.conditionValues.putAll(conditionValues);
            conditionValues.clear();
            conditionValues.putAll(conditionValueSet.conditionValues);
            mergeMixIn(conditionValueSet);
            mergeDefinition(conditionValueSet);
            setIntently(isIntently() || conditionValueSet.isIntently());
            return this;
        }

        private void mergeMixIn(ConditionValueSet another) {
            if (mixIns.length != 0 && another.mixIns.length != 0
                    && !new HashSet<>(asList(mixIns)).equals(new HashSet<>(asList(another.mixIns))))
                throw new IllegalArgumentException(String.format("Cannot merge different mix-in %s and %s for %s.%s",
                        Arrays.toString(mixIns), Arrays.toString(another.mixIns), beanClass.getName(), property));
            if (mixIns.length == 0)
                mixIns = another.mixIns;
        }

        private void mergeDefinition(ConditionValueSet<T> another) {
            if (definition != null && another.definition != null
                    && !Objects.equals(definition, another.definition))
                throw new IllegalArgumentException(String.format("Cannot merge different definition `%s` and `%s` for %s.%s",
                        definition, another.definition, beanClass.getName(), property));
            if (definition == null)
                definition = another.definition;
        }
    }

    private static class CollectionConditionValue<T> extends ConditionValue<T> {
        private final Map<Integer, ConditionValue<T>> conditionValueIndexMap = new LinkedHashMap<>();

        public CollectionConditionValue(int index, ConditionValue<T> conditionValue, String property, BeanClass<T> beanClass) {
            super(property, beanClass);
            conditionValueIndexMap.put(index, conditionValue);
        }

        @Override
        public boolean matches(BeanClass<?> type, Object propertyValue) {
            List<Object> elements = BeanClass.arrayCollectionToStream(propertyValue).collect(Collectors.toList());
            return conditionValueIndexMap.entrySet().stream()
                    .allMatch(e -> e.getValue().matches(type, elements.get(e.getKey())));
        }

        @Override
        public Producer<?> buildProducer(FactorySet factorySet, Producer<T> parent, Instance<T> instance) {
            CollectionProducer<?, ?> producer = getCollectionProducer(factorySet.getObjectFactorySet(), parent, instance);
            conditionValueIndexMap.forEach((k, v) -> producer.addChild(k, v.buildProducer(factorySet, parent, instance)));
            return producer;
        }

        private CollectionProducer<?, ?> getCollectionProducer(ObjectFactorySet objectFactorySet, Producer<T> parent, Instance<T> instance) {
            CollectionProducer<?, ?> producer = (CollectionProducer<?, ?>) parent.getChild(property);
            if (producer == null)
                producer = new CollectionProducer<>(objectFactorySet, parent.getType().getPropertyWriter(property), instance);
            return producer;
        }

        @Override
        public ConditionValue<T> merge(ConditionValue<T> conditionValue) {
            return conditionValue.mergeTo(this);
        }

        @Override
        protected ConditionValue<T> mergeTo(CollectionConditionValue<T> collectionConditionValue) {
            collectionConditionValue.conditionValueIndexMap.forEach((k, v) ->
                    conditionValueIndexMap.put(k, conditionValueIndexMap.containsKey(k) ?
                            conditionValueIndexMap.get(k).merge(v)
                            : v));
            return this;
        }
    }
}

