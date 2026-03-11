package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;
import org.testcharm.util.CollectionHelper;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.testcharm.jfactory.DefaultBuilder.BuildFrom.SPEC;
import static org.testcharm.util.function.Extension.firstPresent;

class DefaultBuilder<T> implements Builder<T> {
    enum BuildFrom {
        TYPE, SPEC
    }

    private final ObjectFactory<T> objectFactory;
    private final JFactory jFactory;
    private final Set<String> traits = new LinkedHashSet<>();

    private final List<SubBuilder> properties;
    private final DefaultArguments arguments = new DefaultArguments();
    private int collectionSize = 0;
    private final BuildFrom buildFrom;

    private Optional<Association> association = Optional.empty();
    private Optional<ReverseAssociation> reverseAssociation = Optional.empty();

    //    TODO refactor
    DefaultBuilder<T> setAssociation(Optional<Association> association) {
        DefaultBuilder<T> clone = clone();
        clone.association = association;
        return clone;
    }

    DefaultBuilder<T> setReverseAssociation(Optional<ReverseAssociation> reverseAssociation) {
        DefaultBuilder<T> clone = clone();
        clone.reverseAssociation = reverseAssociation;
        return clone;
    }

    public DefaultBuilder(ObjectFactory<T> objectFactory, JFactory jFactory, BuildFrom buildFrom) {
        this.jFactory = jFactory;
        this.objectFactory = objectFactory;
        properties = new ArrayList<>();
        this.buildFrom = buildFrom;
    }

    @Override
    public T create() {
        ObjectProducer<T> producer = createProducer();
        T value = producer.processConsistent().getValue();
        producer.verifyPropertyStructureDependent();
        return value;
    }

    @Override
    public BeanClass<T> getType() {
        return objectFactory.getType();
    }

    public ObjectProducer<T> createProducer() {
        return new ObjectProducer<>(jFactory, objectFactory, this, association, reverseAssociation);
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
        builder.properties.addAll(properties);
        builder.traits.addAll(traits);
        builder.arguments.merge(arguments);
        builder.reverseAssociation = reverseAssociation;
        builder.association = association;
        return builder;
    }

    @Override
    public Builder<T> properties(Map<String, ?> properties) {
        DefaultBuilder<T> newBuilder = clone();
        properties.forEach((key, value) -> {
            String property = replaceStartsWithIndexBracket(jFactory.aliasSetStore.resolve(
                    objectFactory, key, isCollection(value)), newBuilder);
            if (isCollection(value)) {
                List<Object> objects = CollectionHelper.convertToStream(value).collect(Collectors.toList());
                if (objects.isEmpty() || !property.contains("$"))
                    newBuilder.properties.add(SubBuilder.create(trimIndexAlias(property), value, null, objectFactory));
                else for (int i = 0; i < objects.size(); i++)
                    newBuilder.properties.add(SubBuilder.create(property.replaceFirst("\\$", String.valueOf(i)), objects.get(i), null, objectFactory));
            } else
                newBuilder.properties.add(SubBuilder.create(property, value, null, objectFactory));
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
        Matcher<T> objectMatcher = new Matcher<>(SubBuilder.groupByProperty(properties));
        return jFactory.getDataRepository().queryAll(objectFactory.getType().getType()).stream()
                .filter(object -> objectMatcher.matches(object, objectFactory)).collect(Collectors.toList());
    }

    @Override
    public T query() {
        List<T> list = new ArrayList<>(queryAll());
        if (!properties.isEmpty() && list.size() > 1)
            throw new IllegalStateException("There are multiple elements in the query result.");
        return list.stream().findFirst().orElse(null);
    }

    public void collectSpec(ObjectProducer<T> objectProducer, SpecRules<T> rules) {
        objectFactory.collectSpec(traits, rules);
        rules.applySpecs(jFactory, objectProducer);
        objectProducer.processSpecIgnoreProperties();
    }

    public void processInputProperty(ObjectProducer<T> producer) {
        SubBuilder.groupByProperty(properties).stream().map(subBuilder -> processReverseAssociation(producer, subBuilder)).forEach(subBuilder ->
//                        TODO top list transformer
                producer.changeChild(subBuilder.property(), subBuilder.buildProducer(producer, objectFactory, jFactory)));
    }

    private SubBuilder processReverseAssociation(ObjectProducer<T> producer, SubBuilder subBuilder) {
        return subBuilder instanceof SubValueBuilder ? subBuilder
                : producer.isReverseAssociation(subBuilder.property()) ? subBuilder.forceCreate() : subBuilder;
    }

    public DefaultBuilder<T> marge(DefaultBuilder<T> another) {
        ObjectFactory<T> objectFactory = another.buildFrom == SPEC && another.objectFactory instanceof SpecClassFactory
                ? another.objectFactory : this.objectFactory;
        DefaultBuilder<T> newBuilder = clone(objectFactory, BuildFrom.TYPE);
        newBuilder.properties.addAll(another.properties);
        newBuilder.traits.addAll(another.traits);
        newBuilder.collectionSize = collectionSize;
        newBuilder.association = firstPresent(association, another.association);
        newBuilder.reverseAssociation = firstPresent(reverseAssociation, another.reverseAssociation);
        return newBuilder;
    }

    public DefaultArguments getArguments() {
        return arguments;
    }
}
