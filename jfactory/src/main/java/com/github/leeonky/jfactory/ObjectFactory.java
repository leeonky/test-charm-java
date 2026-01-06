package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class ObjectFactory<T> implements Factory<T> {
    protected final FactorySet factorySet;
    private final BeanClass<T> type;
    private final Map<String, Consumer<Spec<T>>> traitCollectors = new LinkedHashMap<>();
    private final Map<String, Pattern> traitPatternCollectors = new LinkedHashMap<>();
    private final Map<String, Transformer> transformers = new LinkedHashMap<>();
    private final Transformer passThrough = input -> input;
    private Function<Instance<T>, T> constructor = this::defaultConstruct;
    private Consumer<Spec<T>> specCollector = (instance) -> {
    };

    public ObjectFactory(BeanClass<T> type, FactorySet factorySet) {
        this.type = type;
        this.factorySet = factorySet;
    }

    @SuppressWarnings("unchecked")
    private T defaultConstruct(Instance<T> instance) {
        return getType().isCollection()
                ? (T) getType().createCollection(Collections.nCopies(instance.collectionSize(), getType().getElementType().createDefault()))
                : getType().newInstance();
    }

    protected Spec<T> createSpec() {
        return new Spec<T>() {
            @Override
            public BeanClass<T> getType() {
                return type;
            }
        };
    }

    @Override
    public final Factory<T> constructor(Function<Instance<T>, T> constructor) {
        this.constructor = Objects.requireNonNull(constructor);
        return this;
    }

    @Override
    public Factory<T> spec(Consumer<Spec<T>> specCollector) {
        this.specCollector = Objects.requireNonNull(specCollector);
        return this;
    }

    @Override
    public Factory<T> spec(String name, Consumer<Spec<T>> traitCollector) {
        traitCollectors.put(name, Objects.requireNonNull(traitCollector));
        traitPatternCollectors.put(name, Pattern.compile(name));
        return this;
    }

    public final T create(Instance<T> instance) {
        instance.getSequence();
        return constructor.apply(instance);
    }

    @Override
    public BeanClass<T> getType() {
        return type;
    }

    @Override
    public Factory<T> transformer(String property, Transformer transformer) {
        transformers.put(property, transformer);
        return this;
    }

    private static class TraitExecutor<T> {
        private final Consumer<Spec<T>> action;
        private final List<Object> args = new ArrayList<>();

        public TraitExecutor(Matcher matcher, Consumer<Spec<T>> action) {
            for (int i = 0; i < matcher.groupCount(); i++)
                args.add(matcher.group(i + 1));
            this.action = action;
        }

        public TraitExecutor(Consumer<Spec<T>> action) {
            this.action = action;
        }

        public void execute(Instance<T> instance) {
            ((ObjectInstance<T>) instance).runTraitWithParams(args.toArray(), action);
        }
    }

    public void collectSpec(Collection<String> traits, ObjectInstance<T> instance) {
        specCollector.accept(instance.spec);
        collectSubSpec(instance);
        for (String name : traits)
            queryTrait(name).execute(instance);
    }

    private TraitExecutor<T> queryTrait(String name) {
        Consumer<Spec<T>> action = traitCollectors.get(name);
        if (action != null)
            return new TraitExecutor<>(action);
        List<Matcher> matchers = traitPatternCollectors.values().stream().map(p -> p.matcher(name)).filter(Matcher::matches)
                .collect(Collectors.toList());
        if (matchers.size() == 1)
            return new TraitExecutor<>(matchers.get(0), traitCollectors.get(matchers.get(0).pattern().pattern()));
        if (matchers.size() > 1)
            throw new IllegalArgumentException(String.format("Ambiguous trait pattern: %s, candidates are:\n%s", name,
                    matchers.stream().map(p -> "  " + p.pattern().pattern()).collect(Collectors.joining("\n"))));
        throw new IllegalArgumentException("Trait `" + name + "` not exist");
    }

    protected void collectSubSpec(ObjectInstance<T> instance) {
    }

    public ObjectInstance<T> createInstance(Arguments argument, Optional<Association> association,
                                            Optional<ReverseAssociation> reverseAssociation,
                                            ObjectProducer<T> objectProducer) {
        Spec<T> spec = createSpec();
        ObjectInstance<T> objectInstance = new ObjectInstance<>(spec, argument, factorySet.sequence(getType().getType()));
        SpecRules<T> rules = new SpecRules<>(objectInstance, objectProducer, association, reverseAssociation);
        spec.setRules(rules);
        return objectInstance;
    }

    public FactorySet getFactorySet() {
        return factorySet;
    }

    public ObjectFactory<T> getBase() {
        return this;
    }

    public Object transform(String name, Object value) {
        return queryTransformer(name, () -> passThrough).checkAndTransform(value);
    }

    protected Transformer queryTransformer(String name, Supplier<Transformer> fallback) {
        return transformers.getOrDefault(name, fallback(name, fallback).get());
    }

    protected Supplier<Transformer> fallback(String name, Supplier<Transformer> fallback) {
        return () -> getType().getType().getSuperclass() == null ? fallback.get()
                : factorySet.queryObjectFactory(BeanClass.create(getType().getType().getSuperclass()))
                .queryTransformer(name, fallback);
    }
}
