package com.github.leeonky.jfactory;

import com.github.leeonky.util.PropertyWriter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static com.github.leeonky.util.Sneaky.cast;
import static java.util.stream.IntStream.range;

class ObjectProducer<T> extends Producer<T> {
    private final ObjectFactory<T> factory;
    private final JFactory jFactory;
    private final DefaultBuilder<T> builder;
    private final RootInstance<T> instance;
    private final Map<String, Producer<?>> children = new HashMap<>();
    private final Map<PropertyChain, String> reverseAssociations = new LinkedHashMap<>();
    private final ListPersistable cachedChildren = new ListPersistable();
    private final Set<String> ignorePropertiesInSpec = new HashSet<>();
    private Persistable persistable;
    private Function<PropertyWriter<?>, Producer<?>> defaultListElementValueProducerFactory;
    private final ConsistencySet consistencySet = new ConsistencySet();

    public JFactory jFactory() {
        return jFactory;
    }

    public ObjectProducer(JFactory jFactory, ObjectFactory<T> factory, DefaultBuilder<T> builder) {
        this(jFactory, factory, builder, false);
    }

    public ObjectProducer(JFactory jFactory, ObjectFactory<T> factory, DefaultBuilder<T> builder, boolean forQuery) {
        super(factory.getType());
        this.factory = factory;
        this.jFactory = jFactory;
        this.builder = builder;
        instance = factory.createInstance(builder.getArguments());
        persistable = jFactory.getDataRepository();
        defaultListElementValueProducerFactory = propertyWriter -> new DefaultValueFactoryProducer<>(factory.getType(),
                factory.getFactorySet().getDefaultValueFactory(propertyWriter.getType()),
                instance.sub(propertyWriter));
        createDefaultValueProducers();
        builder.collectSpec(this, instance);
        builder.processInputProperty(this, forQuery);
        setupReverseAssociations();
        resolveBuilderProducers();
    }

    protected void resolveBuilderProducers() {
        List<Map.Entry<String, Producer<?>>> buildValueProducers = children.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof BuilderValueProducer).collect(Collectors.toList());
        buildValueProducers.forEach(e -> setChild(e.getKey(), ((BuilderValueProducer) e.getValue()).getProducer()));
    }

    private void createElementDefaultValueProducersWhenBuildListAsRoot() {
        try {
            children.keySet().stream().map(Integer::valueOf).max(Integer::compareTo).ifPresent(size -> {
                size++;
                instance.setCollectionSize(size);
                range(0, size).mapToObj(String::valueOf)
                        .filter(index -> children.get(index) == null)
                        .map(index -> getType().getPropertyWriter(index))
                        .forEach((PropertyWriter<T> propertyWriter) ->
                                setChild(propertyWriter.getName(), defaultListElementValueProducerFactory.apply(propertyWriter)));
            });
        } catch (Exception ignore) {
        }
    }

    private void setupReverseAssociations() {
        reverseAssociations.forEach((child, association) ->
                descendant(child).setupAssociation(association, instance, cachedChildren));
    }

    @Override
    public void setChild(String property, Producer<?> producer) {
        children.put(property, producer);
    }

    @Override
    public Producer<?> childOrDefault(String property) {
        return childOrDefaultCollection(getType().getPropertyWriter(property), false);
    }

    public Producer<?> childOrDefaultCollection(PropertyWriter<T> propertyWriter, boolean force) {
        Producer<?> producer = children.get(propertyWriter.getName());
        if (producer == null && !ignorePropertiesInSpec.contains(propertyWriter.getName())) {
            if (force || propertyWriter.getType().isCollection())
                setChild(propertyWriter.getName(), producer = new CollectionProducer<>(getType(), propertyWriter.getType(),
                        instance.sub(propertyWriter), factory.getFactorySet()));
            else if (!propertyWriter.getType().isCollection())
                producer = createPropertyDefaultValueProducer(propertyWriter)
                        .orElse(new DefaultValueProducer<>(propertyWriter.getType(), () -> cast(propertyWriter.getType().createDefault())));
        }
        return producer;
    }

    @Override
    protected T produce() {
        return instance.cache(() -> {
            createElementDefaultValueProducersWhenBuildListAsRoot();
            return factory.create(instance);
        }, obj -> {
            produceSubs(obj);
            persistable.save(obj);
            cachedChildren.getAll().forEach(persistable::save);
        });
    }

    private void produceSubs(T obj) {
        children.entrySet().stream().filter(this::isDefaultValueProducer).forEach(e -> produceSub(obj, e));
        children.entrySet().stream().filter(e -> !(isDefaultValueProducer(e))).forEach(e -> produceSub(obj, e));
    }

    private void produceSub(T obj, Map.Entry<String, Producer<?>> e) {
        getType().setPropertyValue(obj, e.getKey(), e.getValue().getValue());
    }

    private boolean isDefaultValueProducer(Map.Entry<String, Producer<?>> e) {
        return e.getValue() instanceof DefaultValueFactoryProducer;
    }

    @Override
    public Optional<Producer<?>> child(String property) {
        return Optional.ofNullable(children.get(property));
    }

    public ObjectProducer<T> processConsistent() {
        collectConsistent(this, propertyChain(""));
        consistencySet.resolve(this);
        return this;
    }

    @Override
    protected void collectConsistent(ObjectProducer<?> root, PropertyChain base) {
        if (root != this)
            root.consistencySet.addAll(consistencySet.absoluteProperty(base));
        children.forEach((property, producer) -> producer.collectConsistent(root, base.concat(property)));
    }

    private void createDefaultValueProducers() {
        getType().getPropertyWriters().values().stream().filter(jFactory::shouldCreateDefaultValue)
                .forEach(propertyWriter -> createPropertyDefaultValueProducer(propertyWriter)
                        .ifPresent(producer -> setChild(propertyWriter.getName(), producer)));
    }

    @Override
    public Optional<Producer<?>> createPropertyDefaultValueProducer(PropertyWriter<?> property) {
        return factory.getFactorySet().queryDefaultValueFactory(property.getType())
                .map(builder -> new DefaultValueFactoryProducer<>(getType(), builder, instance.sub(property)));
    }

    @Override
    public Producer<T> changeTo(Producer<T> newProducer) {
        return newProducer.changeFrom(this);
    }

    @Override
    protected Producer<T> changeFrom(ObjectProducer<T> origin) {
        return origin.builder.marge(builder).createProducer();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Producer<T> changeFrom(OptionalSpecDefaultValueProducer<T> producer) {
        if (producer.getTraitsAndSpec() != null)
            return ((DefaultBuilder<T>) jFactory.spec(producer.getTraitsAndSpec())).marge(builder).createProducer();
        return this;
    }

    public void appendReverseAssociation(PropertyChain property, String association) {
        reverseAssociations.put(property, association);
    }

    @Override
    protected <T> void setupAssociation(String association, RootInstance<T> instance, ListPersistable cachedChildren) {
        setChild(association, new UnFixedValueProducer<>(instance.reference(), instance.spec().getType()));
        persistable = cachedChildren;
    }

    public boolean isReverseAssociation(String property) {
        return reverseAssociations.containsKey(PropertyChain.propertyChain(property));
    }

    public void ignoreProperty(String property) {
        ignorePropertiesInSpec.add(property);
    }

    public void processSpecIgnoreProperties() {
        children.entrySet().stream().filter(e -> e.getValue() instanceof DefaultValueProducer
                        && ignorePropertiesInSpec.contains(e.getKey())).map(Map.Entry::getKey).collect(Collectors.toList())
                .forEach(children::remove);
    }

    @Override
    protected boolean isFixed() {
        return children.values().stream().anyMatch(Producer::isFixed);
    }

    public void changeElementDefaultValueProducerFactory(Function<PropertyWriter<?>, Producer<?>> factory) {
        defaultListElementValueProducerFactory = factory;
    }

    public void appendLink(DefaultConsistency<?> consistency) {
        consistencySet.add(consistency);
    }
}
