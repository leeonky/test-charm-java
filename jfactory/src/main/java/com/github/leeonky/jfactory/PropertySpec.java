package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.leeonky.util.CollectionHelper.reify;
import static com.github.leeonky.util.Sneaky.cast;
import static java.lang.String.format;
import static java.util.Optional.of;

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
        return appendProducer(context ->
                new UnFixedValueProducer<>(value, (BeanClass<V>) context.producer.getPropertyWriterType(context.property)));
    }

    public <V> Spec<T> is(Class<? extends Spec<V>> specClass) {
        if (isAssociation())
            return spec;
        return appendProducer(context -> createCreateProducer(context.jFactory.spec(specClass), context.association));
    }

    private boolean isAssociation() {
        return spec.isAssociation(property.toString())
                || spec.isReverseAssociation(property.isTopLevelPropertyCollection() ? property.removeTail() : property);
    }

    public Spec<T> is(String... traitsAndSpec) {
        if (isAssociation())
            return spec;
        return appendProducer(context -> createCreateProducer(context.jFactory.spec(traitsAndSpec), context.association));
    }

    public <V> IsSpec2<V> from(String... traitsAndSpec) {
        return spec.newIsSpec(traitsAndSpec, this);
    }

    public Spec<T> optional(String... traitsAndSpec) {
        if (property.isSingle()) {
            return spec.appendSpec((jFactory, objectProducer) -> objectProducer.changeChild(property.toString(),
                    new OptionalSpecDefaultValueProducer<>(traitsAndSpec, jFactory, jFactory.spec(traitsAndSpec))));
        } else if (property.isDefaultPropertyCollection()) {
            return spec.appendSpec((jFactory, objectProducer) -> {
                PropertyWriter<T> propertyWriter = objectProducer.getType().getPropertyWriter((String) property.head());
                if (!propertyWriter.getType().isCollection() && propertyWriter.getType().is(Object.class)) {
                    Factory<Object> factory = jFactory.specFactory(traitsAndSpec[traitsAndSpec.length - 1]);
                    propertyWriter = propertyWriter.decorateType(reify(List.class, factory.getType().getGenericType()));
                } else if (propertyWriter.getType().isCollection() && propertyWriter.getType().getElementType().is(Object.class)) {
                    Factory<Object> factory = jFactory.specFactory(traitsAndSpec[traitsAndSpec.length - 1]);
                    propertyWriter = propertyWriter.decorateType(reify(propertyWriter.getType().getType(), factory.getType().getGenericType()));
                }
                CollectionProducer<?, ?> collectionProducer = BeanClass.cast(objectProducer.forceChildOrDefaultCollection(propertyWriter),
                        CollectionProducer.class).orElseThrow(() ->
                        new IllegalArgumentException(format("%s.%s is not list", spec.getType().getName(), property.head())));
                OptionalSpecDefaultValueProducer<?> optionalSpecDefaultValueProducer =
                        new OptionalSpecDefaultValueProducer<>(traitsAndSpec, jFactory, jFactory.spec(traitsAndSpec));
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
        return appendProducer((context) ->
                new DefaultValueProducer<>((BeanClass<V>) context.producer.getPropertyWriterType(context.property), supplier));
    }

    public Spec<T> byFactory() {
        if (isAssociation())
            return spec;
        return appendProducer(context ->
                context.producer.newDefaultValueProducer(cast(context.producer.getType().getPropertyWriter(context.property))).orElseGet(() ->
                        createCreateProducer(context.jFactory.type(context.producer.getPropertyWriterType(context.property).getType()), context.association)));
    }

    public Spec<T> byFactory(Function<Builder<?>, Builder<?>> builder) {
        if (isAssociation())
            return spec;
        return appendProducer(context ->
                context.producer.newDefaultValueProducer(cast(context.producer.getType().getPropertyWriter(context.property))).orElseGet(() ->
                        createQueryOrCreateProducer(builder.apply(context.jFactory.type(
                                context.producer.getPropertyWriterType(context.property).getType())), context.association)));
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

    private Spec<T> appendProducer(Function<ProducerFactoryContext, Producer<?>> producerFactory) {
        if (property.isSingle() || property.isTopLevelPropertyCollection())
            return spec.appendSpec((jFactory, objectProducer) -> objectProducer.changeDescendant(property,
                    ((nextToLast, property) -> producerFactory.apply(new ProducerFactoryContext(jFactory, nextToLast, property,
                            objectProducer.association(this.property.head().toString()), objectProducer)))));
        if (property.isDefaultPropertyCollection()) {
            return spec.appendSpec((jFactory, objectProducer) -> {
                PropertyWriter<T> propertyWriter = objectProducer.getType().getPropertyWriter((String) property.head());
                if (!propertyWriter.getType().isCollection() && propertyWriter.getType().is(Object.class)) {
                    Producer<?> element = producerFactory.apply(new ProducerFactoryContext(jFactory, objectProducer, "0", objectProducer.association(property.head().toString()), objectProducer));
                    propertyWriter = propertyWriter.decorateType(reify(List.class, element.getType().getGenericType()));
                }
                CollectionProducer<?, ?> collectionProducer = BeanClass.cast(objectProducer.forceChildOrDefaultCollection(propertyWriter),
                        CollectionProducer.class).orElseThrow(() ->
                        new IllegalArgumentException(format("%s.%s is not list", spec.getType().getName(), property.head())));
                collectionProducer.changeElementPopulationFactory(index ->
                        producerFactory.apply(new ProducerFactoryContext(jFactory, collectionProducer, index.getName(), objectProducer.association(property.head().toString()), objectProducer)));
            });
        }
        if (property.isTopLevelDefaultPropertyCollection()) {
            return spec.appendSpec((jFactory, objectProducer) -> {
                objectProducer.changeElementPopulationFactory(propertyWriter ->
                        producerFactory.apply(new ProducerFactoryContext(jFactory, objectProducer, propertyWriter.getName(), Optional.empty(), objectProducer)));
            });
        }
        throw new IllegalArgumentException(format("Property chain `%s` is not supported in the current operation", property));
    }

    private <V> Producer<V> createQueryOrCreateProducer(Builder<V> builder, Optional<Association> association) {
        DefaultBuilder<V> builderWithArgs = ((DefaultBuilder<V>) builder.args(spec.params(property.toString())))
                .setAssociation(association).setReverseAssociation(of(new ReverseAssociation(property.toString(), spec.instance())));
        return new BuilderValueProducer<>(builderWithArgs, true);
    }

    private <V> Producer<V> createCreateProducer(Builder<V> builder, Optional<Association> association) {
        DefaultBuilder<V> args = (DefaultBuilder<V>) builder.args(spec.params(property.toString()));
        args = args.setAssociation(association).setReverseAssociation(of(new ReverseAssociation(property.toString(), spec.instance())));
        return new BuilderValueProducer<>(args, false);
    }

    public Spec<T> reverseAssociation(String association) {
        spec.objectProducer.appendReverseAssociation(property, association);
        return spec;
    }

    public Spec<T> ignore() {
        return spec.appendSpec((jFactory, objectProducer) -> objectProducer.ignoreProperty(property.toString()));
    }

    public PropertySpec<T> element(int index) {
        return spec.property(property.toString() + "[" + index + "]");
    }

    @FunctionalInterface
    interface Fuc<P1, P2, P3, P4, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4);
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
            if (isAssociation())
                return spec;
            return appendProducer(context -> createCreateProducer(context.jFactory.spec(specClass, trait), context.association));
        }

        public Spec<T> and(Function<Builder<V>, Builder<V>> builder) {
            spec.consume(this);
            if (isAssociation())
                return spec;
            return appendProducer(context -> createQueryOrCreateProducer(builder.apply(context.jFactory.spec(specClass)), context.association));
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
            if (isAssociation())
                return PropertySpec.this.spec;
            return appendProducer(context -> createQueryOrCreateProducer(builder.apply(context.jFactory.spec(spec)), context.association));
        }

        public String getPosition() {
            return position;
        }
    }
}

class ProducerFactoryContext {
    final JFactory jFactory;
    final Producer<?> producer;
    final String property;
    final Optional<Association> association;
    final ObjectProducer<?> objectProducer;

    ProducerFactoryContext(JFactory jFactory, Producer<?> producer,
                           String property, Optional<Association> association,
                           ObjectProducer<?> objectProducer) {
        this.jFactory = jFactory;
        this.producer = producer;
        this.property = property;
        this.association = association;
        this.objectProducer = objectProducer;
    }
}
