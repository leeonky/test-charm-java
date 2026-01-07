package com.github.leeonky.jfactory;

import com.github.leeonky.util.Converter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.leeonky.util.Classes.newInstance;
import static com.github.leeonky.util.Sneaky.execute;

class SpecClassFactory<T> extends ObjectFactory<T> {
    private final Class<? extends Spec<T>> specClass;
    private final Supplier<ObjectFactory<T>> base;

    public SpecClassFactory(Class<? extends Spec<T>> specClass, FactorySet factorySet, boolean globalSpec) {
        super(newInstance(specClass).getType(), factorySet);
        this.specClass = specClass;
        base = guessBaseFactory(factorySet, globalSpec);
        registerTraits();
        constructor(instance -> newSpecInstance().constructBy(this, instance));
    }

    private Supplier<ObjectFactory<T>> guessBaseFactory(FactorySet factorySet, boolean globalSpec) {
        if (!globalSpec)
            return () -> factorySet.queryObjectFactory(getType());
        ObjectFactory<T> typeBaseFactory = factorySet.queryObjectFactory(getType()); // DO NOT INLINE
        return () -> typeBaseFactory;
    }

    @Override
    protected Spec<T> newSpecInstance() {
        return newInstance(specClass);
    }

    private void registerTraits() {
        Stream.of(specClass.getMethods())
                .filter(this::isTraitMethod)
                .forEach(method -> spec(getTraitName(method), spec -> execute(() ->
                        method.invoke(spec, convertParams(method, spec.traitParams())))));
    }

    private Object[] convertParams(Method method, Object[] traitParams) {
        return new ArrayList<Object>() {{
            for (int i = 0; i < method.getParameterTypes().length; i++)
                add(Converter.getInstance().convert(method.getParameterTypes()[i], traitParams[i]));
        }}.toArray();
    }

    private boolean isTraitMethod(Method method) {
        return method.getAnnotation(Trait.class) != null;
    }

    private String getTraitName(Method method) {
        Trait annotation = method.getAnnotation(Trait.class);
        return annotation.value().isEmpty() ? method.getName() : annotation.value();
    }

    @Override
    protected void collectSubSpec(Spec<T> spec_) {
        getBase().collectSpec(Collections.emptyList(), spec_);
        collectClassSpec(Spec::main, spec_);
    }

    protected void collectClassSpec(Consumer<Spec<T>> consumer, Spec<T> spec1) {
        if (spec1.getClass().equals(specClass))
            consumer.accept(spec1);
        else {
            Spec<T> spec = newSpecInstance();
            spec.setRules(spec1.specRules);
            consumer.accept(spec);
            spec1.specRules.append(spec);
        }
    }

    @Override
    public ObjectFactory<T> getBase() {
        return base.get();
    }

    public Class<? extends Spec<T>> getSpecClass() {
        return specClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Supplier<Transformer> fallback(String name, Supplier<Transformer> fallback) {
        return () -> specClass.getSuperclass().equals(Spec.class) ? getBase().queryTransformer(name, fallback)
                : factorySet.querySpecClassFactory((Class<? extends Spec<T>>) specClass.getSuperclass())
                .queryTransformer(name, fallback);
    }
}
