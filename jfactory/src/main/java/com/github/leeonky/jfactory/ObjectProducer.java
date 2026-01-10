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
    private final boolean forQuery;
    private final ObjectInstance<T> instance;
    private final Map<String, Producer<?>> children = new HashMap<>();
    private final Map<PropertyChain, String> reverseAssociations = new LinkedHashMap<>();
    private final ListPersistable cachedChildren = new ListPersistable();
    private final Set<String> ignorePropertiesInSpec = new HashSet<>();
    private Persistable persistable;
    private Function<PropertyWriter<T>, Producer<?>> elementPopulationFactory = any -> null;
    private final ConsistencySet consistencySet = new ConsistencySet();
    private final List<PropertyStructureDependent> propertyStructureDependents = new ArrayList<>();
    private final List<DefaultListStructure<T, ?>> listStructures = new ArrayList<>();
    private boolean autoResolveBuilderValueProducer = false;

    public JFactory jFactory() {
        return jFactory;
    }

    public ObjectProducer(JFactory jFactory, ObjectFactory<T> factory, DefaultBuilder<T> builder,
                          Optional<Association> association, Optional<ReverseAssociation> reverseAssociation) {
        this(jFactory, factory, builder, false, association, reverseAssociation);
    }

    public ObjectProducer(JFactory jFactory, ObjectFactory<T> factory, DefaultBuilder<T> builder, boolean forQuery,
                          Optional<Association> association, Optional<ReverseAssociation> reverseAssociation) {
        super(factory.getType());
        this.factory = factory;
        this.jFactory = jFactory;
        this.forQuery = forQuery;
        instance = factory.createInstance(builder.getArguments(), this, association, reverseAssociation);
        persistable = jFactory.getDataRepository();
        setupDefaultValueProducers();
        builder.collectSpec(this, instance.specRules());
        builder.processInputProperty(this, forQuery);
        resolveBuilderValueProducer(forQuery);
        instance.specRules().applyPropertyStructureDefinitions(jFactory, this, factory);
        processListStructures();
        setupReverseAssociations();

//        reverseAssociation.ifPresent(reverseAssociation1 -> {
//            reverseAssociations.forEach((r, a) -> {
//                if (reverseAssociation1.matches(a, getType().getPropertyWriter(r.toString()).getType().getElementOrPropertyType())) {
//                    if (descendantForRead(r) instanceof CollectionProducer) {
//                        changeDescendant(r.concat(0), (producer, s) -> reverseAssociation1.buildUnFixedValueProducer());
//                    } else
//                        changeDescendant(r, (producer, s) -> reverseAssociation1.buildUnFixedValueProducer());
//                }
//            });
//        });
    }

    private void processListStructures() {
        listStructures.forEach(listStructure -> listStructure.process(this, jFactory));
    }

    public Producer<?> newElementPopulationProducer(PropertyWriter<T> propertyWriter) {
        return getFirstPresent(() -> ofNullable(elementPopulationFactory.apply(propertyWriter)),
                () -> buildPropertyDefaultValueProducer(propertyWriter))
                .orElseGet(() -> new DefaultTypeValueProducer<>(propertyWriter.getType()));
    }

    //        TODO refactor duplicated call
    @Override
    protected Producer<?> resolveBuilderValueProducer(boolean forQuery) {
        autoResolveBuilderValueProducer = true;
        for (Map.Entry<String, Producer<?>> kv : children.entrySet())
            setChild(kv.getKey(), kv.getValue());
        return this;
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
    protected Producer<?> setChild(String property, Producer<?> producer) {
        if (autoResolveBuilderValueProducer)
            producer = producer.resolveBuilderValueProducer(forQuery);
        children.put(property, producer);
        return producer;
    }

    @Override
    public Optional<Producer<?>> getChild(String property) {
        return ofNullable(children.get(property));
    }

    @Override
    public Producer<?> childForUpdate(String property) {
        PropertyWriter<T> propertyWriter = getType().getPropertyWriter(property);
        return getFirstPresent(() -> getChild(propertyWriter.getName()),
                () -> buildPropertyDefaultValueProducer(propertyWriter)).orElseGet(() -> {
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
            children.forEach((key, value) -> getType().setPropertyValue(obj, key, value.getValue()));
            persistable.save(obj);
            cachedChildren.getAll().forEach(persistable::save);
        });
    }

    public ObjectProducer<T> processConsistent() {
        collectConsistent(this, propertyChain(""));
        consistencySet.resolve(this);
        return this;
    }

    @Override
    public void verifyPropertyStructureDependent() {
        for (PropertyStructureDependent propertyStructureDependent : propertyStructureDependents)
            propertyStructureDependent.verify(getValue());
        children.values().forEach(Producer::verifyPropertyStructureDependent);
    }

    @Override
    protected void collectConsistent(ObjectProducer<?> root, PropertyChain base) {
        if (root != this)
            root.consistencySet.addAll(consistencySet.absoluteProperty(base));
        children.forEach((property, producer) -> producer.collectConsistent(root, base.concat(property)));
    }

    private void setupDefaultValueProducers() {
        getType().getPropertyWriters().values().stream().filter(jFactory::shouldCreateDefaultValue)
                .forEach(propertyWriter -> defaultValueProducer(propertyWriter)
                        .ifPresent(producer -> setChild(propertyWriter.getName(), producer)));
    }

    private Optional<Producer<?>> defaultValueProducer(PropertyWriter<T> propertyWriter) {
        return factory.getFactorySet().queryDefaultValueFactory(propertyWriter.getType()).map(valueFactory ->
                new DefaultValueFactoryProducer<>(propertyWriter.getBeanType(), valueFactory, instance.sub(propertyWriter)));
    }

    @Override
    public Optional<Producer<?>> buildPropertyDefaultValueProducer(PropertyWriter<T> property) {
        if (ignorePropertiesInSpec.contains(property.getName()))
            return empty();
        if (property.getType().isCollection())
            return of(createCollectionProducer(property));
        else
            return defaultValueProducer(property);
    }

    public Optional<Producer<?>> newDefaultValueProducerForRead(PropertyWriter<T> property) {
        if (ignorePropertiesInSpec.contains(property.getName()))
            return empty();
        else
            return defaultValueProducer(property);
    }

    private Producer<?> createCollectionProducer(PropertyWriter<T> property) {
        return setChild(property.getName(), new CollectionProducer<>(getType(), property.getType(), instance.sub(property),
                factory.getFactorySet(), jFactory));
    }

    @Override
    public Producer<T> changeTo(Producer<T> newProducer) {
        return newProducer.changeFrom(this);
    }

    public void appendReverseAssociation(PropertyChain property, String association) {
        reverseAssociations.put(property, association);
    }

    @Override
    protected <R> void setupAssociation(String association, ObjectInstance<R> instance, ListPersistable cachedChildren) {
        setChild(association, new UnFixedValueProducer<>(instance.reference(), instance.type()));
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

    public void appendListStructure(DefaultListStructure<T, ?> listStructure) {
        listStructures.add(listStructure);
    }

    public Optional<Association> association(String string) {
        return ofNullable(reverseAssociations.get(propertyChain(string)))
                .map(p -> new Association(p));
    }

    public Optional<String> reverseAssociation(PropertyChain property) {
        return Optional.ofNullable(reverseAssociations.get(property));
    }
}

//TODO refactor
class Association {
    final String property;

    Association(String property) {
        this.property = property;
    }

    boolean matches(String property) {
        return this.property.equals(property);
    }
}

class ReverseAssociation {
    private final String property;
    //    TODO refactor
    final Instance<?> instance;
    private final Spec<?> spec;

    ReverseAssociation(String property, Instance<?> instance, Spec<?> spec) {
        this.property = property;
        this.instance = instance;
        this.spec = spec;
    }

    public boolean matches(String property, BeanClass<?> type) {
        return this.property.equals(property) && spec.getType().equals(type);
    }

    UnFixedValueProducer buildUnFixedValueProducer() {
        return new UnFixedValueProducer(instance.reference(), spec.getType());
    }
}
