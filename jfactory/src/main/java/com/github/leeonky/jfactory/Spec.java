package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.function.BiConsumer;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;

public class Spec<T> {
    SpecRules<T> specRules;

    private ObjectFactory<T> objectFactory;

    T constructBy(ObjectFactory<T> factory) {
        try {
            objectFactory = factory;
            return construct();
        } finally {
            objectFactory = null;
        }
    }

    protected T construct() {
        return objectFactory.getBase().create(specRules.instance());
    }

    public void main() {
    }

    public PropertySpec<T> property(String property) {
        return new PropertySpec<>(this, propertyChain(property));
    }

    Spec<T> appendRule(BiConsumer<JFactory, ObjectProducer<T>> rule) {
        specRules.append(rule);
        return this;
    }

    @SuppressWarnings("unchecked")
    public BeanClass<T> getType() {
        return getClass().equals(Spec.class) ? specRules.type :
                (BeanClass<T>) BeanClass.create(getClass()).getSuper(Spec.class).getTypeArguments(0)
                        .orElseThrow(() -> new IllegalStateException("Cannot guess type via generic type argument, please override Spec::getType"));
    }

    protected String getName() {
        return getClass().getSimpleName();
    }

    Spec<T> setRules(SpecRules<T> rules) {
        specRules = rules;
        return this;
    }

    public Instance<T> instance() {
        return specRules.instance();
    }

    public Spec<T> ignore(String... properties) {
        for (String property : properties)
            property(property).ignore();
        return this;
    }

    @Deprecated
    <V, S extends Spec<V>> PropertySpec<T>.IsSpec<V, S> newIsSpec(Class<S> specClass, PropertySpec<T> propertySpec) {
        return specRules.newIsSpec(specClass, propertySpec);
    }

    @Deprecated
    void consume(PropertySpec<T>.IsSpec<?, ? extends Spec<?>> isSpec) {
        specRules.consume(isSpec);
    }

    <V> PropertySpec<T>.IsSpec2<V> newIsSpec(String[] traitsAndSpec, PropertySpec<T> propertySpec) {
        return specRules.newIsSpec(traitsAndSpec, propertySpec);
    }

    void consume(PropertySpec<T>.IsSpec2<?> isSpec) {
        specRules.consume(isSpec);
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
        appendRule((jFactory, objectProducer) -> objectProducer.appendLink(consistency));
        return consistency;
    }

    public <V, C extends Coordinate> Consistency<V, C> consistent(Class<V> type, Class<C> cType) {
        DefaultConsistency<V, C> consistency = new DefaultConsistency<>(type, cType);
        appendRule((jFactory, objectProducer) -> objectProducer.appendLink(consistency));
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
        appendRule((jFactory, objectProducer) -> objectProducer.appendListStructure(listStructure));
        return listStructure;
    }

    //    TODO needed?

    boolean isAssociation(String property) {
        return specRules.association.map(a -> a.matches(property)).orElse(false);
    }

    boolean isReverseAssociation(PropertyChain property) {
        return specRules.objectProducer.reverseAssociation(property)
                .map(s -> specRules.reverseAssociation.map(a -> a.matches(s,
                        getType().getPropertyWriter(property.toString()).getType().getElementOrPropertyType())).orElse(false))
                .orElse(false);
    }
}
