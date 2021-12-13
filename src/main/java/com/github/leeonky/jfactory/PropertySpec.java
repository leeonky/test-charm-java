package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;

public class PropertySpec<T> {
    private final Spec<T> spec;
    private final PropertyChain property;

    PropertySpec(Spec<T> spec, PropertyChain property) {
        this.spec = spec;
        this.property = property;
    }

    public Spec<T> value(Object value) {
        return value(() -> value);
    }

    @SuppressWarnings("unchecked")
    public <V> Spec<T> value(Supplier<V> value) {
        if (value == null)
            return value(() -> null);
        return appendProducer((factorySet, producer, property) ->
                new UnFixedValueProducer<>(value, (BeanClass<V>) producer.getPropertyWriterType(property)));
    }

    public <V> Spec<T> is(Class<? extends Spec<V>> specClass) {
        return appendProducer(factorySet -> createProducer(factorySet.spec(specClass)));
    }

    public Spec<T> is(String... traitsAndSpec) {
        return appendProducer(factorySet -> createProducer(factorySet.spec(traitsAndSpec)));
    }

    public <V, S extends Spec<V>> IsSpec<V, S> from(Class<S> specClass) {
        return new IsSpec<>(specClass);
    }

    public Spec<T> defaultValue(Object value) {
        return defaultValue(() -> value);
    }

    @SuppressWarnings("unchecked")
    public <V> Spec<T> defaultValue(Supplier<V> supplier) {
        if (supplier == null)
            return defaultValue((Object) null);
        return appendProducer((factorySet, producer, property) ->
                new DefaultValueProducer<>((BeanClass<V>) producer.getPropertyWriterType(property), supplier));
    }

    public Spec<T> byFactory() {
        return byFactory(identity());
    }

    public Spec<T> byFactory(Function<Builder<?>, Builder<?>> builder) {
        return appendProducer((jFactory, producer, property) ->
                producer.subDefaultValueProducer(producer.getType().getPropertyWriter(property))
                        .orElseGet(() -> createProducer(builder.apply(jFactory.type(
                                producer.getPropertyWriterType(property).getType())))));
    }

    public Spec<T> dependsOn(String dependency, Function<Object, Object> rule) {
        return dependsOn(singletonList(dependency), objects -> rule.apply(objects[0]));
    }

    public Spec<T> dependsOn(List<String> dependencies, Function<Object[], Object> rule) {
        return spec.append((factorySet, objectProducer) ->
                objectProducer.addDependency(property, rule,
                        dependencies.stream().map(PropertyChain::createChain).collect(Collectors.toList())));
    }

    private Spec<T> appendProducer(Fuc<JFactory, Producer<?>, String, Producer<?>> producerFactory) {
        if (property.isSingle() || property.isTopLevelPropertyCollection())
            return spec.append((factorySet, objectProducer) -> objectProducer.changeDescendant(property,
                    ((nextToLast, property) -> producerFactory.apply(factorySet, nextToLast, property))));
        throw new IllegalArgumentException(format("Not support property chain '%s' in current operation", property));
    }

    private Spec<T> appendProducer(Function<JFactory, Producer<?>> producerFactory) {
        return appendProducer((jFactory, producer, s) -> producerFactory.apply(jFactory));
    }

    @SuppressWarnings("unchecked")
    private <V> Producer<V> createProducer(Builder<V> builder) {
        Builder<V> builderWithArgs = builder.args(spec.params(property.toString()));
        return builderWithArgs.queryAll().stream().findFirst()
                .<Producer<V>>map(object ->
                        new FixedValueProducer<>((BeanClass<V>) BeanClass.create(object.getClass()), object))
                .orElseGet(builderWithArgs::createProducer);
    }

    public Spec<T> reverseAssociation(String association) {
        return spec.append((factorySet, producer) -> producer.appendReverseAssociation(property, association));
    }

    @SuppressWarnings("unchecked")
    public Spec<T> ignore() {
        return appendProducer((factorySet, producer, property) -> {
            BeanClass<Object> type = (BeanClass<Object>) producer.getPropertyWriterType(property);
            return new DefaultValueProducer<>(type, type::createDefault);
        });
    }

    @FunctionalInterface
    interface Fuc<P1, P2, P3, R> {
        R apply(P1 p1, P2 p2, P3 p3);
    }

    public class IsSpec<V, S extends Spec<V>> {
        private final Class<S> specClass;

        public IsSpec(Class<S> spec) {
            specClass = spec;
        }

        public Spec<T> which(Consumer<S> trait) {
            return appendProducer(factorySet -> createProducer(factorySet.spec(specClass, trait)));
        }

        public Spec<T> and(Function<Builder<V>, Builder<V>> builder) {
            return appendProducer(factorySet -> createProducer(builder.apply(factorySet.spec(specClass))));
        }
    }
}
