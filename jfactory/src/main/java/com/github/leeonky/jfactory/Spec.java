package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

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
        return new PropertySpec<>(this, propertyChain(property), specRules);
    }

    @SuppressWarnings("unchecked")
    public BeanClass<T> getType() {
        return getClass().equals(Spec.class) ? specRules.runtimeType() :
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
        specRules.append((jFactory, objectProducer) -> objectProducer.appendLink(consistency));
        return consistency;
    }

    public <V, C extends Coordinate> Consistency<V, C> consistent(Class<V> type, Class<C> cType) {
        DefaultConsistency<V, C> consistency = new DefaultConsistency<>(type, cType);
        specRules.append((jFactory, objectProducer) -> objectProducer.appendLink(consistency));
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
        specRules.append((jFactory, objectProducer) -> objectProducer.appendListStructure(listStructure));
        return listStructure;
    }

}
