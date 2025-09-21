package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.github.leeonky.jfactory.PropertyChain.propertyChain;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class Spec<T> {
    private final List<BiConsumer<JFactory, ObjectProducer<T>>> operations = new ArrayList<>();
    private final Set<PropertySpec<T>.IsSpec<?, ? extends Spec<?>>> invalidIsSpecs = new LinkedHashSet<>();
    private final Set<PropertySpec<T>.IsSpec2<?>> invalidIsSpec2s = new LinkedHashSet<>();

    private Instance<T> instance;
    private BeanClass<T> type = null;

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

    Spec<T> append(BiConsumer<JFactory, ObjectProducer<T>> operation) {
        operations.add(operation);
        return this;
    }

    void apply(JFactory jFactory, ObjectProducer<T> producer) {
        operations.forEach(o -> o.accept(jFactory, producer));
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

    @SuppressWarnings("unchecked")
    public BeanClass<T> getType() {
        return getClass().equals(Spec.class) ? type :
                (BeanClass<T>) BeanClass.create(getClass()).getSuper(Spec.class).getTypeArguments(0)
                        .orElseThrow(() -> new IllegalStateException("Cannot guess type via generic type argument, please override Spec::getType"));
    }

    protected String getName() {
        return getClass().getSimpleName();
    }

    @Deprecated
    public Spec<T> link(String property, String... others) {
        List<PropertyChain> linkProperties = concat(of(property), of(others)).map(PropertyChain::propertyChain).collect(toList());
        append((jFactory, objectProducer) -> objectProducer.link(linkProperties));
        return this;
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
        operations.addAll(spec.operations);
        invalidIsSpecs.addAll(spec.invalidIsSpecs);
    }

    public Spec<T> linkNew(String propertyChain1, String propertyChain2, String... others) {
        Consistency<?> consistency = consistent(Object.class);
        consistency.direct(propertyChain1)
                .direct(propertyChain2);
        for (String string : others)
            consistency.direct(string);
        return this;
    }

    public <V> Consistency<V> consistent(Class<V> type) {
        DefaultConsistency<V> consistency = new DefaultConsistency<>(type);
        append((jFactory, objectProducer) -> objectProducer.appendLink(consistency));
        return consistency;
    }
}
