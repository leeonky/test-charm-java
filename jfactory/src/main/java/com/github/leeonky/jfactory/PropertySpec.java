package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

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
        return appendProducer((jFactory, producer, property) ->
                new UnFixedValueProducer<>(value, (BeanClass<V>) producer.getPropertyWriterType(property)));
    }

    public <V> Spec<T> is(Class<? extends Spec<V>> specClass) {
        return appendProducer(jFactory -> createCreateProducer(jFactory.spec(specClass)));
    }

    public Spec<T> is(String... traitsAndSpec) {
        return appendProducer(jFactory -> createCreateProducer(jFactory.spec(traitsAndSpec)));
    }

    public <V, S extends Spec<V>> IsSpec<V, S> from(Class<S> specClass) {
        return spec.newIsSpec(specClass, this);
    }

    public Spec<T> defaultValue(Object value) {
        return defaultValue(() -> value);
    }

    @SuppressWarnings("unchecked")
    public <V> Spec<T> defaultValue(Supplier<V> supplier) {
        if (supplier == null)
            return defaultValue((Object) null);
        return appendProducer((jFactory, producer, property) ->
                new DefaultValueProducer<>((BeanClass<V>) producer.getPropertyWriterType(property), supplier));
    }

    public Spec<T> byFactory() {
        return appendProducer((jFactory, producer, property) ->
                producer.createPropertyDefaultValueProducer(producer.getType().getPropertyWriter(property)).orElseGet(() ->
                        createCreateProducer(jFactory.type(producer.getPropertyWriterType(property).getType()))));
    }

    public Spec<T> byFactory(Function<Builder<?>, Builder<?>> builder) {
        return appendProducer((jFactory, producer, property) ->
                producer.createPropertyDefaultValueProducer(producer.getType().getPropertyWriter(property))
                        .orElseGet(() -> createQueryOrCreateProducer(builder.apply(jFactory.type(
                                producer.getPropertyWriterType(property).getType())))));
    }

    public Spec<T> dependsOn(String dependency, Function<Object, Object> rule) {
        return dependsOn(singletonList(dependency), objects -> rule.apply(objects[0]));
    }

    public Spec<T> dependsOn(List<String> dependencies, Function<Object[], Object> rule) {
        return spec.append((jFactory, objectProducer) ->
                objectProducer.addDependency(property, rule,
                        dependencies.stream().map(PropertyChain::propertyChain).collect(Collectors.toList())));
    }

    private Spec<T> appendProducer(Fuc<JFactory, Producer<?>, String, Producer<?>> producerFactory) {
        if (property.isSingle() || property.isTopLevelPropertyCollection())
            return spec.append((jFactory, objectProducer) -> {
                objectProducer.changeDescendant(property, ((nextToLast, property) -> producerFactory.apply(jFactory, nextToLast, property)));
            });
        if (property.isDefaultPropertyCollection()) {
            return spec.append((jFactory, objectProducer) -> {
                CollectionProducer<?, ?> collectionProducer = BeanClass.cast(objectProducer.childOrDefault((String) property.head()),
                        CollectionProducer.class).orElseThrow(() ->
                        new IllegalArgumentException(format("%s.%s is not list", spec.getType().getName(), property.head())));
                collectionProducer.changeElementDefaultValueProducerFactory(index ->
                        producerFactory.apply(jFactory, collectionProducer, index.toString()));
            });
        }
        throw new IllegalArgumentException(format("Not support property chain '%s' in current operation", property));
    }

    private Spec<T> appendProducer(Function<JFactory, Producer<?>> producerFactory) {
        return appendProducer((jFactory, producer, s) -> producerFactory.apply(jFactory));
    }

    @SuppressWarnings("unchecked")
    private <V> Producer<V> createQueryOrCreateProducer(Builder<V> builder) {
        Builder<V> builderWithArgs = builder.args(spec.params(property.toString()));
        return builderWithArgs.queryAll().stream().findFirst().<Producer<V>>map(object ->
                        new BuilderValueProducer<>((BeanClass<V>) BeanClass.create(object.getClass()), builderWithArgs))
                .orElseGet(builderWithArgs::createProducer);
    }

    private <V> Producer<V> createCreateProducer(Builder<V> builder) {
        return builder.args(spec.params(property.toString())).createProducer();
    }

    public Spec<T> reverseAssociation(String association) {
        return spec.append((jFactory, producer) -> producer.appendReverseAssociation(property, association));
    }

    public Spec<T> ignore() {
        return spec.append((jFactory, objectProducer) -> objectProducer.ignoreProperty(property.toString()));
    }

    @FunctionalInterface
    interface Fuc<P1, P2, P3, R> {
        R apply(P1 p1, P2 p2, P3 p3);
    }

    public class IsSpec<V, S extends Spec<V>> {
        private final Class<S> specClass;
        private final String position;

        public IsSpec(Class<S> spec) {
            position = Thread.currentThread().getStackTrace()[4].toString();
            specClass = spec;
        }

        public Spec<T> which(Consumer<S> trait) {
            spec.consume(this);
            return appendProducer(jFactory -> createCreateProducer(jFactory.spec(specClass, trait)));
        }

        public Spec<T> and(Function<Builder<V>, Builder<V>> builder) {
            spec.consume(this);
            return appendProducer(jFactory -> createQueryOrCreateProducer(builder.apply(jFactory.spec(specClass))));
        }

        public String getPosition() {
            return position;
        }
    }
}
