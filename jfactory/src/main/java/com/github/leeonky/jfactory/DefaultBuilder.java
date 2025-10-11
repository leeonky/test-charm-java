package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.CollectionHelper;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.leeonky.jfactory.DefaultBuilder.BuildFrom.SPEC;
import static com.github.leeonky.util.BeanClass.cast;
import static java.util.Arrays.asList;
import static java.util.Objects.hash;

class DefaultBuilder<T> implements Builder<T> {
    enum BuildFrom {
        TYPE, SPEC
    }

    private final ObjectFactory<T> objectFactory;
    private final JFactory jFactory;
    private final Set<String> traits = new LinkedHashSet<>();

    private final KeyValueCollection properties;
    private final DefaultArguments arguments = new DefaultArguments();
    private int collectionSize = 0;
    private final BuildFrom buildFrom;

    public DefaultBuilder(ObjectFactory<T> objectFactory, JFactory jFactory, BuildFrom buildFrom) {
        this.jFactory = jFactory;
        this.objectFactory = objectFactory;
        properties = new KeyValueCollection(objectFactory.getFactorySet());
        this.buildFrom = buildFrom;
    }

    @Override
    public T create() {
        ObjectProducer<T> producer = createProducer(Optional.empty());
        T value = producer.processConsistent().getValue();
        producer.verifyPropertyStructureDependent();
        return value;
    }

    @Override
    public BeanClass<T> getType() {
        return objectFactory.getType();
    }

    @Override
    public ObjectProducer<T> createProducer(Optional<Association> association) {
        return new ObjectProducer<>(jFactory, objectFactory, this, association);
    }

    @Override
    public Builder<T> arg(String key, Object value) {
        DefaultBuilder<T> newBuilder = clone();
        newBuilder.arguments.put(key, value);
        return newBuilder;
    }

    @Override
    public Builder<T> args(Arguments arguments) {
        DefaultBuilder<T> newBuilder = clone();
        newBuilder.arguments.merge((DefaultArguments) arguments);
        return newBuilder;
    }

    @Override
    public Builder<T> args(Map<String, ?> args) {
        DefaultBuilder<T> newBuilder = clone();
        args.forEach(newBuilder.arguments::put);
        return newBuilder;
    }

    @Override
    public Builder<T> args(String property, Map<String, Object> args) {
        DefaultBuilder<T> newBuilder = clone();
        args.forEach((key, value) -> newBuilder.arguments.put(property, key, value));
        return newBuilder;
    }

    @Override
    public Builder<T> traits(String... traits) {
        DefaultBuilder<T> newBuilder = clone();
        newBuilder.traits.addAll(asList(traits));
        return newBuilder;
    }

    @Override
    public DefaultBuilder<T> clone() {
        return clone(objectFactory, buildFrom);
    }

    private DefaultBuilder<T> clone(ObjectFactory<T> objectFactory, BuildFrom from) {
        DefaultBuilder<T> builder = new DefaultBuilder<>(objectFactory, jFactory, from);
        builder.properties.appendAll(properties);
        builder.traits.addAll(traits);
        builder.arguments.merge(arguments);
        return builder;
    }

    @Override
    public Builder<T> properties(Map<String, ?> properties) {
        DefaultBuilder<T> newBuilder = clone();
        properties.forEach((key, value) -> {
            String property;
            property = replaceStartsWithIndexBracket(jFactory.aliasSetStore.resolve(
                    objectFactory, key, isCollection(value)), newBuilder);
            if (isCollection(value)) {
                List<Object> objects = CollectionHelper.toStream(value).collect(Collectors.toList());
                if (objects.isEmpty() || !property.contains("$"))
                    newBuilder.properties.append(trimIndexAlias(property), objects);
                else for (int i = 0; i < objects.size(); i++)
                    newBuilder.properties.append(property.replaceFirst("\\$", String.valueOf(i)), objects.get(i));
            } else
                newBuilder.properties.append(property, value);
        });
        return newBuilder;
    }

    private String trimIndexAlias(String property) {
        if (property.contains("[$]"))
            return property.substring(0, property.indexOf("[$]"));
        return property;
    }

    private boolean isCollection(Object value) {
        return value != null && BeanClass.createFrom(value).isCollection();
    }

    private String replaceStartsWithIndexBracket(String key, DefaultBuilder<T> newBuilder) {
        if (key.startsWith("[")) {
            String[] indexAndSub = key.substring(1).split("]", 2);
            newBuilder.collectionSize = Math.max(newBuilder.collectionSize, Integer.parseInt(indexAndSub[0]) + 1);
            return indexAndSub[0] + indexAndSub[1];
        }
        return key;
    }

    @Override
    public Collection<T> queryAll() {
        ObjectProducer<T> producer = new ObjectProducer<>(jFactory, objectFactory, this, true, Optional.empty());
        KeyValueCollection.Matcher<T> matcher = properties.matcher(objectFactory.getType(), objectFactory, producer);
        return jFactory.getDataRepository().queryAll(objectFactory.getType().getType()).stream()
                .filter(matcher::matches).collect(Collectors.toList());
    }

    @Override
    public T query() {
        List<T> list = new ArrayList<>(queryAll());
        if (!properties.isEmpty() && list.size() > 1)
            throw new IllegalStateException("There are multiple elements in the query result.");
        return list.stream().findFirst().orElse(null);
    }

    @Override
    public int hashCode() {
        return hash(DefaultBuilder.class, objectFactory, properties, traits);
    }

    @Override
    public boolean equals(Object another) {
        return cast(another, DefaultBuilder.class)
                .map(builder -> objectFactory.equals(builder.objectFactory) && properties.equals(builder.properties)
                        && traits.equals(builder.traits))
                .orElseGet(() -> super.equals(another));
    }

    public void collectSpec(ObjectProducer<T> objectProducer, RootInstance<T> instance) {
        objectFactory.collectSpec(traits, instance);
        instance.spec().applySpecs(jFactory, objectProducer);
        objectProducer.processSpecIgnoreProperties();
    }

    public void processInputProperty(ObjectProducer<T> producer, boolean forQuery) {
        properties.expressions(objectFactory.getType(), objectFactory, producer, forQuery).forEach(exp -> producer.changeChild(exp.getProperty(),
                intentlyCreateWhenReverseAssociation(producer, exp).buildProducer(jFactory, producer)));
    }

    private Expression<T> intentlyCreateWhenReverseAssociation(ObjectProducer<T> producer, Expression<T> exp) {
        return exp instanceof SingleValueExpression ? exp :
                producer.isReverseAssociation(exp.getProperty()) ? exp.setIntently(true) : exp;
    }

    public DefaultBuilder<T> marge(DefaultBuilder<T> another) {
        ObjectFactory<T> objectFactory = another.buildFrom == SPEC && another.objectFactory instanceof SpecClassFactory
                ? another.objectFactory : this.objectFactory;
        DefaultBuilder<T> newBuilder = clone(objectFactory, BuildFrom.TYPE);
        newBuilder.properties.appendAll(another.properties);
        newBuilder.traits.addAll(another.traits);
        newBuilder.collectionSize = collectionSize;
        return newBuilder;
    }

    public DefaultArguments getArguments() {
        return arguments;
    }
}
