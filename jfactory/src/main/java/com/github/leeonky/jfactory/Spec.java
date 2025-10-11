package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;

public class Spec<T> {
    private final List<BiConsumer<JFactory, ObjectProducer<T>>> specDefinitions = new ArrayList<>();
    private final List<PropertyStructureDefinition<T>> propertyStructureDefinitions = new ArrayList<>();
    private final Set<PropertySpec<T>.IsSpec<?, ? extends Spec<?>>> invalidIsSpecs = new LinkedHashSet<>();
    private final Set<PropertySpec<T>.IsSpec2<?>> invalidIsSpec2s = new LinkedHashSet<>();

    private Instance<T> instance;
    private BeanClass<T> type = null;
    Optional<Association> association = Optional.empty();

    private ObjectFactory<T> objectFactory;

    T constructBy(ObjectFactory<T> factory) {
        objectFactory = factory;
        try {
            if (objectFactory == null)
                throw new IllegalStateException("Illegal construct context");
            return construct();
        } finally {
            objectFactory = null;
        }
    }

    protected T construct() {
        return objectFactory.getBase().create(instance);
    }

    public void main() {
    }

    public PropertySpec<T> property(String property) {
        return new PropertySpec<>(this, propertyChain(property));
    }

    Spec<T> appendSpec(BiConsumer<JFactory, ObjectProducer<T>> operation) {
        specDefinitions.add(operation);
        return this;
    }

    void applySpecs(JFactory jFactory, ObjectProducer<T> producer) {
        specDefinitions.forEach(o -> o.accept(jFactory, producer));
        type = producer.getType();
        if (!invalidIsSpecs.isEmpty())
            throw new InvalidSpecException("Invalid property spec:\n\t"
                    + invalidIsSpecs.stream().map(PropertySpec.IsSpec::getPosition).collect(Collectors.joining("\n\t"))
                    + "\nShould finish method chain with `and` or `which`:\n"
                    + "\tproperty().from().which()\n"
                    + "\tproperty().from().and()\n"
                    + "Or use property().is() to create object with only spec directly.");
        if (!invalidIsSpec2s.isEmpty())
            throw new InvalidSpecException("Invalid property spec:\n\t"
                    + invalidIsSpec2s.stream().map(PropertySpec.IsSpec2::getPosition).collect(Collectors.joining("\n\t"))
                    + "\nShould finish method chain with `and`:\n"
                    + "\tproperty().from().and()\n"
                    + "Or use property().is() to create object with only spec directly.");
    }

    void applyPropertyStructureDefinitions(JFactory jFactory, ObjectProducer<T> producer) {
        specDefinitions.clear();
        for (PropertyStructureDefinition<T> propertyStructureDefinition : propertyStructureDefinitions)
            propertyStructureDefinition.apply(this, producer);
        applySpecs(jFactory, producer);
    }

    @SuppressWarnings("unchecked")
    public BeanClass<T> getType() {
        return getClass().equals(Spec.class) ? type :
                (BeanClass<T>) BeanClass.create(getClass()).getSuper(Spec.class).getTypeArguments(0)
                        .orElseThrow(() -> new IllegalStateException("Cannot guess type via generic type argument, please override Spec::getType"));
    }

    protected String getName() {
        return getClass().getSimpleName();
    }

    Spec<T> setInstance(Instance<T> instance) {
        this.instance = instance;
        return this;
    }

    public <P> P param(String key) {
        return instance.param(key);
    }

    public <P> P param(String key, P defaultValue) {
        return instance.param(key, defaultValue);
    }

    public Arguments params(String property) {
        return instance.params(property);
    }

    public Arguments params() {
        return instance.params();
    }

    public Instance<T> instance() {
        return instance;
    }

    public Spec<T> ignore(String... properties) {
        for (String property : properties)
            property(property).ignore();
        return this;
    }

    @Deprecated
    <V, S extends Spec<V>> PropertySpec<T>.IsSpec<V, S> newIsSpec(Class<S> specClass, PropertySpec<T> propertySpec) {
        PropertySpec<T>.IsSpec<V, S> isSpec = propertySpec.new IsSpec<V, S>(specClass);
        invalidIsSpecs.add(isSpec);
        return isSpec;
    }

    @Deprecated
    void consume(PropertySpec<T>.IsSpec<?, ? extends Spec<?>> isSpec) {
        invalidIsSpecs.remove(isSpec);
    }

    <V> PropertySpec<T>.IsSpec2<V> newIsSpec(String[] traitsAndSpec, PropertySpec<T> propertySpec) {
        PropertySpec<T>.IsSpec2<V> isSpec = propertySpec.new IsSpec2<V>(traitsAndSpec);
        invalidIsSpec2s.add(isSpec);
        return isSpec;
    }

    void consume(PropertySpec<T>.IsSpec2<?> isSpec) {
        invalidIsSpec2s.remove(isSpec);
    }

    void append(Spec<T> spec) {
        specDefinitions.addAll(spec.specDefinitions);
        invalidIsSpecs.addAll(spec.invalidIsSpecs);
    }

    public Spec<T> link(String propertyChain1, String propertyChain2, String... others) {
        Consistency<?, ?> consistency = consistent(Object.class);
        consistency.direct(propertyChain1)
                .direct(propertyChain2);
        for (String string : others)
            consistency.direct(string);
        return this;
    }

    public <V> Consistency<V, Coordinate> consistent(Class<V> type) {
        DefaultConsistency<V, Coordinate> consistency = new DefaultConsistency<>(type, Coordinate.class);
        appendSpec((jFactory, objectProducer) -> objectProducer.appendLink(consistency));
        return consistency;
    }

    public <V, C extends Coordinate> Consistency<V, C> consistent(Class<V> type, Class<C> cType) {
        DefaultConsistency<V, C> consistency = new DefaultConsistency<>(type, cType);
        appendSpec((jFactory, objectProducer) -> objectProducer.appendLink(consistency));
        return consistency;
    }

    public PropertyStructureBuilder<T> structure(String property) {
        return new PropertyStructureBuilder<>(this, property);
    }

    public ListStructure<T, Coordinate> structure() {
        return structure(Coordinate.class);
    }

    public <C extends Coordinate> ListStructure<T, C> structure(Class<C> coordinateType) {
        DefaultListStructure<T, C> listStructure = new DefaultListStructure<>(coordinateType);
        appendSpec((jFactory, objectProducer) -> objectProducer.appendListStructure(listStructure));
        return listStructure;
    }

    void appendStructureDefinition(PropertyStructureDefinition<T> propertyStructureDefinition) {
        propertyStructureDefinitions.add(propertyStructureDefinition);
    }

    boolean isAssociation(String property) {
        return association.map(a -> a.matches(property)).orElse(false);
    }
}
