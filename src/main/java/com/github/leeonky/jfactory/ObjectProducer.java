package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

class ObjectProducer<T> extends Producer<T> {
    private final ObjectFactory<T> objectFactory;
    private final FactorySet factorySet;
    private final Instance<T> instance;
    private final Map<String, Producer<?>> children = new HashMap<>();
    private Map<PropertyChain, Dependency<?>> dependencies = new LinkedHashMap<>();

    public ObjectProducer(FactorySet factorySet, ObjectFactory<T> objectFactory, DefaultBuilder<T> builder) {
        super(objectFactory.getType());
        this.objectFactory = objectFactory;
        this.factorySet = factorySet;
        instance = objectFactory.createInstance(factorySet.getTypeSequence());
        establishProducers(factorySet, builder);
    }

    private void establishProducers(FactorySet factorySet, DefaultBuilder<T> builder) {
        buildPropertyValueProducers(factorySet.getObjectFactorySet());
        buildProducersFromSpec(builder);
        buildProducerFromInputProperties(factorySet, builder);
    }

    private void buildProducerFromInputProperties(FactorySet factorySet, DefaultBuilder<T> builder) {
        builder.toExpressions().forEach((p, exp) -> addChild(p, exp.buildProducer(factorySet, this, instance.sub(p))));
    }

    private void buildProducersFromSpec(DefaultBuilder<T> builder) {
        builder.collectSpec(instance);
        instance.spec().apply(factorySet, this);
    }

    private void buildPropertyValueProducers(ObjectFactorySet objectFactorySet) {
        getType().getPropertyWriters().forEach((name, propertyWriter) ->
                objectFactorySet.queryPropertyValueFactory(propertyWriter.getType()).ifPresent(propertyValueFactory ->
                        addChild(name, new PropertyValueProducer<>(getType(), propertyValueFactory, instance.sub(name)))));
    }

    @Override
    public void addChild(String property, Producer<?> producer) {
        children.put(property, producer);
    }

    @Override
    public Producer<?> getChild(String property) {
        Producer<?> producer = children.get(property);
        if (producer == null) {
            BeanClass<?> propertyType = getType().getPropertyWriter(property).getType();
            if (propertyType.isCollection()) {
                addChild(property, producer = new CollectionProducer<>(
                        factorySet.getObjectFactorySet(), getType(), propertyType, instance.sub(property)));
            }
        }
        return producer;
    }

    @Override
    protected T produce() {
        T obj = objectFactory.create(instance);
        instance.giveValue(obj);
        children.forEach((property, producer) -> getType().setPropertyValue(obj, property, producer.getValue()));
        factorySet.getDataRepository().save(obj);
        return obj;
    }

    public void addDependency(PropertyChain property, Function<Object[], Object> function, List<PropertyChain> propertyChains) {
        Dependency<?> dependency = new Dependency<>(function, property, propertyChains);
        dependencies.put(dependency.getProperty(), dependency);
    }

    public ObjectProducer<T> processSpec() {
        dependencies.values().forEach(dependency -> dependency.process(this, instance));
        return this;
    }
}
