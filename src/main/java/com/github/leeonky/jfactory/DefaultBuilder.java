package com.github.leeonky.jfactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.github.leeonky.util.BeanClass.cast;
import static java.util.Arrays.asList;
import static java.util.Objects.hash;

class DefaultBuilder<T> implements Builder<T> {
    private final ObjectFactory<T> objectFactory;
    private final FactorySet factorySet;
    private final Set<String> mixIns = new LinkedHashSet<>();
    private final TypeProperties<T> typeProperties;

    public DefaultBuilder(ObjectFactory<T> objectFactory, FactorySet factorySet) {
        this.factorySet = factorySet;
        this.objectFactory = objectFactory;
        typeProperties = new TypeProperties<>(objectFactory.getType());
    }

    @Override
    public T create() {
        return createProducer(null, false).processDependencyAndLink().getValue();
    }

    @Override
    public ObjectProducer<T> createProducer(String property, boolean intently) {
        return new ObjectProducer<>(factorySet, objectFactory, this, intently);
    }

    @Override
    public Builder<T> mixIn(String... mixIns) {
        DefaultBuilder<T> newBuilder = copy();
        newBuilder.mixIns.addAll(asList(mixIns));
        return newBuilder;
    }

    private DefaultBuilder<T> copy() {
        DefaultBuilder<T> builder = new DefaultBuilder<>(objectFactory, factorySet);
        builder.typeProperties.merge(typeProperties);
        builder.mixIns.addAll(mixIns);
        return builder;
    }

    @Override
    public Builder<T> properties(Map<String, ?> properties) {
        DefaultBuilder<T> newBuilder = copy();
        newBuilder.typeProperties.putAll(properties);
        return newBuilder;
    }

    @Override
    public T query() {
        return queryAll().stream().findFirst().orElse(null);
    }

    @Override
    public Collection<T> queryAll() {
        return typeProperties.select(factorySet.getDataRepository().queryAll(objectFactory.getType().getType()));
    }

    public void collectSpec(Instance<T> instance) {
        objectFactory.collectSpec(mixIns, instance);
    }

    public Collection<PropertyExpression<T>> toExpressions() {
        return typeProperties.toExpressions();
    }

    @Override
    public int hashCode() {
        return hash(DefaultBuilder.class, typeProperties, mixIns);
    }

    @Override
    public boolean equals(Object another) {
        return cast(another, DefaultBuilder.class)
                .map(builder -> typeProperties.equals(builder.typeProperties) && mixIns.equals(builder.mixIns))
                .orElseGet(() -> super.equals(another));
    }

    public void establishProducers(ObjectProducer<T> parent, Instance<T> instance) {
        forDefaultValue(parent, instance);
        forSpec(parent, instance);
        forInputProperties(parent);
    }

    private void forSpec(ObjectProducer<T> parent, Instance<T> instance) {
        collectSpec(instance);
        instance.spec().apply(factorySet, parent);
    }

    private void forInputProperties(ObjectProducer<T> parent) {
        toExpressions().forEach(exp -> parent.addChild(exp.getProperty(), exp.buildProducer(factorySet, parent)));
    }

    private void forDefaultValue(ObjectProducer<T> parent, Instance<T> instance) {
        parent.getType().getPropertyWriters().forEach((name, propertyWriter) ->
                factorySet.getObjectFactorySet().queryDefaultValueFactory(propertyWriter.getType()).ifPresent(propertyValueFactory ->
                        parent.addChild(name, new DefaultValueProducer<>(parent.getType(), propertyValueFactory, instance.sub(name)))));
    }
}
