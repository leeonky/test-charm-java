package com.github.leeonky.jfactory;

import java.util.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

class ConsistencyItem<T> {
    private final Set<PropertyChain> properties;
    private final Consistency<T> consistency;
    private final StackTraceElement location;
    private StackTraceElement composerLocation;
    private StackTraceElement decomposerLocation;
    private DefaultConsistency.Composer<T> composer;
    private DefaultConsistency.Decomposer<T> decomposer;

    ConsistencyItem(Collection<PropertyChain> properties, Consistency<T> consistency) {
        this(properties, consistency, guessCustomerPositionStackTrace());
    }

    ConsistencyItem(Collection<PropertyChain> properties, Consistency<T> consistency, StackTraceElement location) {
        this.properties = new LinkedHashSet<>(properties);
        this.consistency = consistency;
        this.location = location;
    }

    static StackTraceElement guessCustomerPositionStackTrace() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        return Arrays.stream(stackTrace).filter(s -> !s.getClassName().startsWith("com.github.leeonky.jfactory"))
                .findFirst().orElse(stackTrace[0]);
    }

    private static boolean isSame(DefaultConsistency.Identity identity1, DefaultConsistency.Identity identity2) {
        return identity1 != null && identity2 != null && identity1.same(identity2);
    }

    private static boolean isBothNull(DefaultConsistency.Identity identity1, DefaultConsistency.Identity identity2) {
        return identity1 == null && identity2 == null;
    }

    void setComposer(DefaultConsistency.Composer<T> composer) {
        this.composer = composer;
        composerLocation = composer.getLocation();
    }

    void setDecomposer(DefaultConsistency.Decomposer<T> decomposer) {
        this.decomposer = decomposer;
        decomposerLocation = decomposer.getLocation();
    }

    boolean same(ConsistencyItem<?> another) {
        return properties.equals(another.properties) &&
                (isSame(composer, another.composer) && isSame(decomposer, another.decomposer)
                        || isBothNull(composer, another.composer) && isSame(decomposer, another.decomposer)
                        || isSame(composer, another.composer) && isBothNull(decomposer, another.decomposer));
    }

    private String getPosition() {
        return location.getClassName() + "." + location.getMethodName() +
                "(" + location.getFileName() + ":" + location.getLineNumber() + ")";
    }

    private String composerLocation() {
        return composerLocation == null ? "null" :
                "(" + composerLocation.getFileName() + ":" + composerLocation.getLineNumber() + ")";
    }

    private String decomposerLocation() {
        return decomposerLocation == null ? "null" :
                "(" + decomposerLocation.getFileName() + ":" + decomposerLocation.getLineNumber() + ")";
    }

    public ConsistencyItem<T> absoluteProperty(PropertyChain base) {
        ConsistencyItem<T> absolute = new ConsistencyItem<>(properties.stream().map(base::concat).collect(toList()), consistency, location);
        absolute.decomposer = decomposer;
        absolute.composer = composer;
        absolute.decomposerLocation = decomposerLocation;
        absolute.composerLocation = composerLocation;
        return absolute;
    }

    @Override
    public String toString() {
        return properties.stream().map(Objects::toString).collect(joining(", ")) +
                " => " + consistency.type().getName() +
                (composer != null ? " with composer" : "") +
                (decomposer != null ? " with decomposer" : "");
    }

    Resolver resolver(ObjectProducer<?> root, DefaultConsistency<T>.Resolver consistency) {
        return new Resolver(root, consistency);
    }

    class Resolver {
        private final ObjectProducer<?> root;
        private final DefaultConsistency<T>.Resolver consistency;
        private Object[] cached;

        Resolver(ObjectProducer<?> root, DefaultConsistency<T>.Resolver consistency) {
            this.root = root;
            this.consistency = consistency;
        }

        boolean hasTypeOf(Class<?> type) {
            return properties.stream().map(root::descendantForRead).anyMatch(type::isInstance);
        }

        Set<PropertyChain> resolveAsProvider() {
            if (hasTypeOf(PlaceHolderProducer.class))
                return Collections.emptySet();
            return consistency.resolve(this);
        }

        private T compose() {
            return composer.apply(properties.stream().map(root::descendantForRead).map(Producer::getValue).toArray());
        }

        Object[] decompose(Resolver provider) {
            if (cached == null)
                cached = decomposer.apply(provider.compose());
            return cached;
        }

        boolean hasComposer() {
            return composer != null;
        }

        boolean hasDecomposer() {
            return decomposer != null;
        }

        Set<PropertyChain> resolve(Resolver provider) {
            int i = 0;
            for (PropertyChain property : properties) {
                int index = i++;
                root.changeDescendant(property, (producer, s) ->
                        new ConsistencyProducer<>(root.descendantForUpdate(property), provider, this, index));
            }
            return properties;
        }

        @Override
        public int hashCode() {
            return Objects.hash(properties, composer == null ? null : composer.identity(),
                    decomposer == null ? null : decomposer.identity());
        }

        private ConsistencyItem<T> outer() {
            return ConsistencyItem.this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            return o instanceof ConsistencyItem.Resolver && same(((Resolver) o).outer());
        }

        boolean hasFixed() {
            return properties.stream().map(root::descendantForRead).anyMatch(Producer::isFixed);
        }

        boolean containsProperty(PropertyChain property) {
            return properties.contains(property);
        }

        DefaultConsistency<T>.Resolver consistencyResolver() {
            return consistency;
        }
    }
}
