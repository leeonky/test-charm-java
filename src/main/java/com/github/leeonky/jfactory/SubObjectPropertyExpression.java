package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;

import static java.util.Arrays.asList;

class SubObjectPropertyExpression<H, B> extends PropertyExpression<H, B> {
    private final Map<String, Object> conditionValues = new LinkedHashMap<>();
    private String[] mixIns;
    private String definition;

    public SubObjectPropertyExpression(String condition, Object value, String[] mixIns, String definition,
                                       String property, BeanClass<H> hostClass, BeanClass<B> beanClass) {
        super(property, hostClass, beanClass);
        this.mixIns = mixIns;
        this.definition = definition;
        conditionValues.put(condition, value);
    }

    @Override
    protected <P> boolean isMatch(BeanClass<P> propertyType, P propertyValue) {
        return conditionValues.entrySet().stream()
                .map(conditionValue -> ExpressionParser.parse(propertyType, conditionValue.getKey(), conditionValue.getValue()))
                .allMatch(queryExpression -> queryExpression.isMatch(propertyValue));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Producer<?> buildProducer(FactorySet factorySet, Producer<H> host, Instance<B> instance) {
        BeanClass<?> propertyType = hostClass.getPropertyWriter(property).getType();
        if (isIntently())
            return toBuilder(factorySet, propertyType).createProducer(property, true);
        Collection<?> collection = toBuilder(factorySet, hostClass.getPropertyReader(property).getType()).queryAll();
        if (collection.isEmpty())
            return toBuilder(factorySet, propertyType).createProducer(property, false);
        else
            return new FixedValueProducer(propertyType, collection.iterator().next());
    }

    private Builder<?> toBuilder(FactorySet factorySet, BeanClass<?> propertyType) {
        return (definition != null ? factorySet.spec(definition) : factorySet.type(propertyType.getType()))
                .mixIn(mixIns).properties(conditionValues);
    }

    @Override
    public PropertyExpression<H, B> merge(PropertyExpression<H, B> propertyExpression) {
        return propertyExpression.mergeBy(this);
    }

    @Override
    protected PropertyExpression<H, B> mergeBy(SubObjectPropertyExpression<H, B> conditionValueSet) {
        conditionValueSet.conditionValues.putAll(conditionValues);
        conditionValues.clear();
        conditionValues.putAll(conditionValueSet.conditionValues);
        mergeMixIn(conditionValueSet);
        mergeDefinition(conditionValueSet);
        setIntently(isIntently() || conditionValueSet.isIntently());
        return this;
    }

    private void mergeMixIn(SubObjectPropertyExpression another) {
        if (mixIns.length != 0 && another.mixIns.length != 0
                && !new HashSet<>(asList(mixIns)).equals(new HashSet<>(asList(another.mixIns))))
            throw new IllegalArgumentException(String.format("Cannot merge different mix-in %s and %s for %s.%s",
                    Arrays.toString(mixIns), Arrays.toString(another.mixIns), hostClass.getName(), property));
        if (mixIns.length == 0)
            mixIns = another.mixIns;
    }

    private void mergeDefinition(SubObjectPropertyExpression<H, B> another) {
        if (definition != null && another.definition != null
                && !Objects.equals(definition, another.definition))
            throw new IllegalArgumentException(String.format("Cannot merge different definition `%s` and `%s` for %s.%s",
                    definition, another.definition, hostClass.getName(), property));
        if (definition == null)
            definition = another.definition;
    }
}
