package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

import java.util.*;

import static java.util.Arrays.asList;

//TODO final fields
class TraitsSpec {
    private String spec;
    private final Set<String> traits = new LinkedHashSet<>();
    private boolean collectionSpec = false;

    public TraitsSpec(String[] traits, String spec) {
        setSpec(spec);
        this.traits.addAll(asList(traits));
    }

    public TraitsSpec(String traitsAndSpec) {
        String[] items = traitsAndSpec.replace('(', ' ').replace(')', ' ').trim().split(", |,| ");
        setSpec(items[items.length - 1]);
        traits.addAll(asList(items).subList(0, items.length - 1));
    }

    public TraitsSpec() {
    }

    private void mergeTraits(TraitsSpec another) {
        traits.addAll(another.traits);
    }

    private void mergeSpec(TraitsSpec another, String property) {
        if (isDifferentSpec(another))
            throw new IllegalArgumentException(String.format("Cannot merge different spec `%s` and `%s` for property %s",
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

    @SuppressWarnings("unchecked")
    public Builder<Object> toBuilder(JFactory jFactory, BeanClass<?> propertyType) {
        return (Builder<Object>) (spec != null ? jFactory.spec(spec) : jFactory.type(propertyType))
                .traits(traits.toArray(new String[0]));
    }

    @Deprecated
    public void merge(TraitsSpec another, String property) {
        mergeTraits(another);
        mergeSpec(another, property);
    }

    public void mergeFrom(TraitsSpec another, String property) {
        Set<String> newTraits = new LinkedHashSet<>();
        newTraits.addAll(another.traits);
        newTraits.addAll(traits);
        traits.clear();
        traits.addAll(newTraits);
        mergeSpec(another, property);
    }

    public Optional<BeanClass<?>> guessPropertyType(FactorySet factorySet) {
        if (spec != null)
            return Optional.of(factorySet.querySpecClassFactory(spec).getType());
        return Optional.empty();
    }

    public boolean isCollectionElementSpec() {
        return collectionSpec;
    }

    public String spec() {
        return spec;
    }

    public String[] traitsSpec() {
        List<String> strings = new ArrayList<>(traits);
        strings.add(spec);
        return strings.toArray(new String[0]);
    }

    public BeanClass<?> resolveElementType(FactorySet factorySet) {
        if (spec != null)
            return factorySet.querySpecClassFactory(spec).getType().getElementType();
        return null;
    }
}
