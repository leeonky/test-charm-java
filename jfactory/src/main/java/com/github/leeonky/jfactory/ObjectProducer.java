package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.util.Optional.*;
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
    private Function<PropertyWriter<T>, Producer<?>> elementPopulationFactory = any -> null;
    private final ConsistencySet consistencySet = new ConsistencySet();
    private final List<PropertyStructureDependent> propertyStructureDependents = new ArrayList<>();

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
        createDefaultValueProducers();
        builder.collectSpec(this, instance);
        builder.processInputProperty(this, forQuery);
        instance.spec.applyPropertyStructureDefinitions(jFactory, this);
        setupReverseAssociations();
        resolveBuilderProducers();
    }

    public Producer<?> newElementPopulationProducer(PropertyWriter<T> propertyWriter) {
        return getFirstPresent(() -> ofNullable(elementPopulationFactory.apply(propertyWriter)),
                () -> newDefaultValueProducer(propertyWriter))
                .orElseGet(() -> new DefaultTypeValueProducer<>(propertyWriter.getType()));
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
                                setChild(propertyWriter.getName(), newElementPopulationProducer(propertyWriter)));
            });
        } catch (Exception ignore) {
        }
    }

    private void setupReverseAssociations() {
        reverseAssociations.forEach((child, association) ->
                descendantForUpdate(child).setupAssociation(association, instance, cachedChildren));
    }

    @Override
    protected void setChild(String property, Producer<?> producer) {
        children.put(property, producer);
    }

    @Override
    public Optional<Producer<?>> getChild(String property) {
        return ofNullable(children.get(property));
    }

    @Override
    public Producer<?> childForUpdate(String property) {
        PropertyWriter<T> propertyWriter = getType().getPropertyWriter(property);
        return getFirstPresent(() -> getChild(propertyWriter.getName()),
                () -> newDefaultValueProducer(propertyWriter)).orElseGet(() -> {
            if (ignorePropertiesInSpec.contains(propertyWriter.getName()))
                return new ReadOnlyProducer<>(this, propertyWriter.getName());
            return new DefaultTypeValueProducer<>(propertyWriter.getType());
        });
    }

    @Override
    public Producer<?> childForRead(String property) {
        PropertyWriter<T> propertyWriter = getType().getPropertyWriter(property);
        return getFirstPresent(() -> getChild(propertyWriter.getName()),
                () -> newDefaultValueProducerForRead(propertyWriter)).orElseGet(() -> {
            if (ignorePropertiesInSpec.contains(propertyWriter.getName()))
                return new ReadOnlyProducer<>(this, propertyWriter.getName());
            return new DefaultTypeValueProducer<>(propertyWriter.getType());
        });
    }

    public Producer<?> forceChildOrDefaultCollection(PropertyWriter<T> propertyWriter) {
        return getChild(propertyWriter.getName()).orElseGet(() -> createCollectionProducer(propertyWriter));
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

    public ObjectProducer<T> processConsistent() {
        collectConsistent(this, propertyChain(""));
        consistencySet.resolve(this);
        return this;
    }

    @SuppressWarnings("unchecked")
    public void verifyPropertyStructureDependent(T value) {
        for (PropertyStructureDependent propertyStructureDependent : propertyStructureDependents) {
            propertyStructureDependent.verify(value);
        }
        BeanClass<T> type = BeanClass.createFrom(value);

        children.forEach((property, producer) ->
                ((Producer<Object>) producer).verifyPropertyStructureDependent(type.getPropertyValue(value, property)));
    }

    @Override
    protected void collectConsistent(ObjectProducer<?> root, PropertyChain base) {
        if (root != this)
            root.consistencySet.addAll(consistencySet.absoluteProperty(base));
        children.forEach((property, producer) -> producer.collectConsistent(root, base.concat(property)));
    }

    private void createDefaultValueProducers() {
        getType().getPropertyWriters().values().stream().filter(jFactory::shouldCreateDefaultValue)
                .forEach(propertyWriter -> factory.getFactorySet().newDefaultValueFactoryProducer(propertyWriter, instance)
                        .ifPresent(producer -> setChild(propertyWriter.getName(), producer)));
    }

    @Override
    public Optional<Producer<?>> newDefaultValueProducer(PropertyWriter<T> property) {
        if (ignorePropertiesInSpec.contains(property.getName()))
            return empty();
        if (property.getType().isCollection()) {
            return of(createCollectionProducer(property));
        } else
            return factory.getFactorySet().newDefaultValueFactoryProducer(property, instance);
    }

    public Optional<Producer<?>> newDefaultValueProducerForRead(PropertyWriter<T> property) {
        if (ignorePropertiesInSpec.contains(property.getName()))
            return empty();
        else
            return factory.getFactorySet().newDefaultValueFactoryProducer(property, instance);
    }

    private Producer<?> createCollectionProducer(PropertyWriter<T> property) {
        Producer<?> producer = new CollectionProducer<>(getType(), property.getType(), instance.sub(property),
                factory.getFactorySet(), jFactory);
        setChild(property.getName(), producer);
        return producer;
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
    protected <R> void setupAssociation(String association, RootInstance<R> instance, ListPersistable cachedChildren) {
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

    public void changeElementPopulationFactory(Function<PropertyWriter<T>, Producer<?>> factory) {
        elementPopulationFactory = factory;
    }

    public void appendLink(DefaultConsistency<?, ?> consistency) {
        consistencySet.add(consistency);
    }

    public void lock(PropertyStructureDependent propertyStructureDependent) {
        propertyStructureDependents.add(propertyStructureDependent);
    }
}
