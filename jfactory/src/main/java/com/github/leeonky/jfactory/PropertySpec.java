package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.GenericBeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;

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

    @Deprecated
    /**
     * reference spec and trait via string
     */
    public <V> Spec<T> is(Class<? extends Spec<V>> specClass) {
        return appendProducer(jFactory -> createCreateProducer(jFactory.spec(specClass)));
    }

    public Spec<T> is(String... traitsAndSpec) {
        return appendProducer(jFactory -> createCreateProducer(jFactory.spec(traitsAndSpec)));
    }

    public <V> IsSpec2<V> from(String... traitsAndSpec) {
        return spec.newIsSpec(traitsAndSpec, this);
    }

    public Spec<T> optional(String... traitsAndSpec) {
        if (property.isSingle()) {
            return spec.append((jFactory, objectProducer) -> objectProducer.changeChild(property.toString(),
                    new OptionalSpecDefaultValueProducer<>(objectProducer.getPropertyWriterType(property.toString()), traitsAndSpec)));
        } else if (property.isDefaultPropertyCollection()) {
            return spec.append((jFactory, objectProducer) -> {
                PropertyWriter<T> propertyWriter = objectProducer.getType().getPropertyWriter((String) property.head());
                if (!propertyWriter.getType().isCollection() && propertyWriter.getType().is(Object.class)) {
                    Factory<Object> factory = jFactory.specFactory(traitsAndSpec[traitsAndSpec.length - 1]);
                    propertyWriter = propertyWriter.decorateType(GenericBeanClass.create(List.class, factory.getType().getGenericType()));
                } else if (propertyWriter.getType().isCollection() && propertyWriter.getType().getElementType().is(Object.class)) {
                    Factory<Object> factory = jFactory.specFactory(traitsAndSpec[traitsAndSpec.length - 1]);
                    propertyWriter = propertyWriter.decorateType(GenericBeanClass.create(propertyWriter.getType().getType(), factory.getType().getGenericType()));
                }
                CollectionProducer<?, ?> collectionProducer = BeanClass.cast(objectProducer.forceChildOrDefaultCollection(propertyWriter),
                        CollectionProducer.class).orElseThrow(() ->
                        new IllegalArgumentException(format("%s.%s is not list", spec.getType().getName(), property.head())));
                OptionalSpecDefaultValueProducer<?> optionalSpecDefaultValueProducer =
                        new OptionalSpecDefaultValueProducer<>(propertyWriter.getType(), traitsAndSpec);
                collectionProducer.changeElementPopulationFactory(index -> optionalSpecDefaultValueProducer);
            });
        }
        throw new IllegalArgumentException(format("Not support property chain '%s' in current operation", property));
    }

    @Deprecated
    /**
     * reference spec and trait via string
     */
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
                producer.newDefaultValueProducer(producer.getType().getPropertyWriter(property)).orElseGet(() ->
                        createCreateProducer(jFactory.type(producer.getPropertyWriterType(property).getType()))));
    }

    public Spec<T> byFactory(Function<Builder<?>, Builder<?>> builder) {
        return appendProducer((jFactory, producer, property) ->
                producer.newDefaultValueProducer(producer.getType().getPropertyWriter(property))
                        .orElseGet(() -> createQueryOrCreateProducer(builder.apply(jFactory.type(
                                producer.getPropertyWriterType(property).getType())))));
    }

    public Spec<T> dependsOn(String dependency) {
        spec.consistent(Object.class)
                .property(property.toString()).write(Function.identity())
                .property(dependency).read(Function.identity());
        return spec;
    }

    public Spec<T> dependsOn(String dependency, Function<Object, Object> rule) {
        spec.consistent(Object.class)
                .property(property.toString()).write(Function.identity())
                .property(dependency).read(rule);
        return spec;
    }

    public Spec<T> dependsOn(List<String> dependencies, Function<Object[], Object> rule) {
        spec.consistent(Object.class)
                .property(property.toString()).write(Function.identity())
                .properties(dependencies.toArray(new String[0])).read(rule);
        return spec;
    }

    private Spec<T> appendProducer(Fuc<JFactory, Producer<?>, String, Producer<?>> producerFactory) {
        if (property.isSingle() || property.isTopLevelPropertyCollection())
            return spec.append((jFactory, objectProducer) -> {
                objectProducer.changeDescendant(property, ((nextToLast, property) -> producerFactory.apply(jFactory, nextToLast, property)));
            });
        if (property.isDefaultPropertyCollection()) {
            return spec.append((jFactory, objectProducer) -> {
                PropertyWriter<T> propertyWriter = objectProducer.getType().getPropertyWriter((String) property.head());
                if (!propertyWriter.getType().isCollection() && propertyWriter.getType().is(Object.class)) {
                    Producer<?> element = producerFactory.apply(jFactory, objectProducer, "0");
                    propertyWriter = propertyWriter.decorateType(GenericBeanClass.create(List.class, element.getType().getGenericType()));
                }
                CollectionProducer<?, ?> collectionProducer = BeanClass.cast(objectProducer.forceChildOrDefaultCollection(propertyWriter),
                        CollectionProducer.class).orElseThrow(() ->
                        new IllegalArgumentException(format("%s.%s is not list", spec.getType().getName(), property.head())));
                collectionProducer.changeElementPopulationFactory(index ->
                        producerFactory.apply(jFactory, collectionProducer, index.getName()));
            });
        }
        if (property.isTopLevelDefaultPropertyCollection()) {
            return spec.append((jFactory, objectProducer) -> {
                objectProducer.changeElementDefaultValueProducerFactory(propertyWriter ->
                        producerFactory.apply(jFactory, objectProducer, propertyWriter.getName()));
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

    public class IsSpec2<V> {
        private final String[] spec;
        private final String position;

        public IsSpec2(String[] spec) {
            position = Thread.currentThread().getStackTrace()[4].toString();
            this.spec = spec;
        }

        public Spec<T> and(Function<Builder<V>, Builder<V>> builder) {
            PropertySpec.this.spec.consume(this);
            return appendProducer(jFactory -> createQueryOrCreateProducer(builder.apply(jFactory.spec(spec))));
        }

        public String getPosition() {
            return position;
        }
    }
}
