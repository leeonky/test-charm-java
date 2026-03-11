package org.testcharm.jfactory;

import org.testcharm.util.BeanClass;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;

class TraitsSpec {
    private final String originalSpec;
    private final String spec;
    private final boolean collectionSpec;
    private final Set<String> traits = new LinkedHashSet<>();

    @Deprecated
    public TraitsSpec(String[] traits, String spec) {
        originalSpec = spec;
        if (originalSpec != null) {
            collectionSpec = spec.endsWith("[]");
            this.spec = spec.replace("[]", "");
        } else {
            collectionSpec = false;
            this.spec = null;
        }
        this.traits.addAll(asList(traits));
    }

    public TraitsSpec(String traitsAndSpec) {
        String[] items = traitsAndSpec.replace('(', ' ').replace(')', ' ').trim().split(", |,| ");
        originalSpec = items[items.length - 1];
        if (originalSpec != null) {
            collectionSpec = originalSpec.endsWith("[]");
            spec = originalSpec.replace("[]", "");
        } else {
            collectionSpec = false;
            spec = null;
        }
        traits.addAll(asList(items).subList(0, items.length - 1));
    }

    public TraitsSpec() {
        originalSpec = null;
        spec = null;
        collectionSpec = false;
    }

    @SuppressWarnings("unchecked")
    public Builder<Object> toBuilder(JFactory jFactory, BeanClass<?> propertyType) {
        return (Builder<Object>) (spec != null ? jFactory.spec(spec) : jFactory.type(propertyType))
                .traits(traits.toArray(new String[0]));
    }

    public TraitsSpec mergeFrom(TraitsSpec another, String property) {
        if (originalSpec != null && another.originalSpec != null && !originalSpec.equals(another.originalSpec))
            throw new IllegalArgumentException(String.format("Cannot merge different spec `%s` and `%s` for property %s",
                    originalSpec, another.originalSpec, property));

        String newSpec = originalSpec == null ? another.originalSpec : originalSpec;
        Set<String> newTraits = new LinkedHashSet<String>() {{
            addAll(another.traits);
            addAll(traits);
        }};
        return new TraitsSpec(newTraits.toArray(new String[0]), newSpec);
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
}
