package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

class SpecRules<T> {
    private final List<BiConsumer<JFactory, ObjectProducer<T>>> rules = new ArrayList<>();
    private final List<PropertyStructureDefinition<T>> propertyStructureRules = new ArrayList<>();
    private final Set<PropertySpec<T>.IsSpec<?, ? extends Spec<?>>> invalidIsSpecs = new LinkedHashSet<>();
    private final Set<PropertySpec<T>.IsSpec2<?>> invalidIsSpec2s = new LinkedHashSet<>();
    Instance<T> instance;
    BeanClass<T> type = null;
    //    TODO to private
    Optional<Association> association;
    Optional<ReverseAssociation> reverseAssociation;
    ObjectProducer<?> objectProducer;

    public SpecRules(ObjectInstance<T> objectInstance, ObjectProducer<?> objectProducer,
                     Optional<Association> association, Optional<ReverseAssociation> reverseAssociation) {
        instance = objectInstance;
        this.objectProducer = objectProducer;
        this.association = association;
        this.reverseAssociation = reverseAssociation;
    }

    public Instance<T> instance() {
        return instance;
    }

    public void append(BiConsumer<JFactory, ObjectProducer<T>> rule) {
        rules.add(rule);
    }

    public void applySpecs(JFactory jFactory, ObjectProducer<T> producer) {
        rules.forEach(o -> o.accept(jFactory, producer));
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

    public void applyPropertyStructureDefinitions(JFactory jFactory, ObjectProducer<T> producer, Spec<T> spec) {
        rules.clear();
        for (PropertyStructureDefinition<T> propertyStructureDefinition : propertyStructureRules)
            propertyStructureDefinition.apply(spec, producer);
        applySpecs(jFactory, producer);
    }

    @Deprecated
    //TODO not needed when singleton SpecRules instance ?
    public void append(Spec<T> spec) {
        rules.addAll(spec.specRules.rules);
        invalidIsSpecs.addAll(spec.specRules.invalidIsSpecs);
    }

    public void appendStructureDefinition(PropertyStructureDefinition<T> propertyStructureDefinition) {
        propertyStructureRules.add(propertyStructureDefinition);
    }

    public <V, S extends Spec<V>> PropertySpec<T>.IsSpec<V, S> newIsSpec(Class<S> specClass, PropertySpec<T> propertySpec) {
        PropertySpec<T>.IsSpec<V, S> isSpec = propertySpec.new IsSpec<V, S>(specClass);
        invalidIsSpecs.add(isSpec);
        return isSpec;
    }

    public void consume(PropertySpec<T>.IsSpec<?, ? extends Spec<?>> isSpec) {
        invalidIsSpecs.remove(isSpec);
    }

    public <V> PropertySpec<T>.IsSpec2<V> newIsSpec(String[] traitsAndSpec, PropertySpec<T> propertySpec) {
        PropertySpec<T>.IsSpec2<V> isSpec = propertySpec.new IsSpec2<V>(traitsAndSpec);
        invalidIsSpec2s.add(isSpec);
        return isSpec;
    }

    public void consume(PropertySpec<T>.IsSpec2<?> isSpec) {
        invalidIsSpec2s.remove(isSpec);
    }
}