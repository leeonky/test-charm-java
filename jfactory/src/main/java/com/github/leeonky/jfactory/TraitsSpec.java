package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;

class TraitsSpec {
    private String spec;
    private final Set<String> traits;
    private boolean collectionSpec = false;

    public TraitsSpec(String[] traits, String spec) {
        setSpec(spec);
        this.traits = new LinkedHashSet<>(asList(traits));
    }

    private void mergeTraits(TraitsSpec another) {
        traits.addAll(another.traits);
    }

    private void mergeSpec(TraitsSpec another, String property) {
        if (isDifferentSpec(another))
            throw new IllegalArgumentException(String.format("Cannot merge different spec `%s` and `%s` for %s",
                    spec, another.spec, property));
        if (spec == null)
            setSpec(another.spec);
    }

    private void setSpec(String spec) {
        if (spec != null) {
            collectionSpec = spec.endsWith("[]");
            this.spec = spec.replace("[]", "");
        }
    }

    private boolean isDifferentSpec(TraitsSpec another) {
        return spec != null && another.spec != null && !Objects.equals(spec, another.spec);
    }

    public Builder<?> toBuilder(JFactory jFactory, BeanClass<?> propertyType) {
        return (spec != null ? jFactory.spec(spec) : jFactory.type(propertyType.getType()))
                .traits(traits.toArray(new String[0]));
    }

    public void merge(TraitsSpec another, String property) {
        mergeTraits(another);
        mergeSpec(another, property);
    }

    public Optional<BeanClass> guessPropertyType(ObjectFactory<?> objectFactory) {
        if (spec != null)
            return Optional.of(objectFactory.getFactorySet().querySpecClassFactory(spec).getType());
        return Optional.empty();
    }

    public boolean isCollectionSpec() {
        return collectionSpec;
    }

    public String spec() {
        return spec;
    }

    public boolean isEmpty() {
        return spec == null;
    }
}
