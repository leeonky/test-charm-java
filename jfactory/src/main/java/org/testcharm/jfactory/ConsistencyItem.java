package org.testcharm.jfactory;

import org.testcharm.util.IndentBuffer;

import java.util.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.testcharm.jfactory.DefaultConsistency.dumpStackTraceElement;

class ConsistencyItem<T> {
    private final Set<PropertyChain> properties;
    private final DefaultConsistency<T, ?> consistency;
    private final StackTraceElement location;
    private StackTraceElement composerLocation;
    private StackTraceElement decomposerLocation;
    private DefaultConsistency.Composer<T> composer;
    private DefaultConsistency.Decomposer<T> decomposer;

    ConsistencyItem(Collection<PropertyChain> properties, DefaultConsistency<T, ?> consistency) {
        this(properties, consistency, guessCustomerPositionStackTrace());
    }

    ConsistencyItem(Collection<PropertyChain> properties, DefaultConsistency<T, ?> consistency, StackTraceElement location) {
        this.properties = new LinkedHashSet<>(properties);
        this.consistency = consistency;
        this.location = location;
    }

    public ConsistencyItem<T> copy(DefaultConsistency<T, ?> newConsistency) {
        ConsistencyItem<T> item = new ConsistencyItem<>(properties, newConsistency, location);
        item.decomposer = decomposer;
        item.composer = composer;
        item.decomposerLocation = decomposerLocation;
        item.composerLocation = composerLocation;
        return item;
    }

    static StackTraceElement guessCustomerPositionStackTrace() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        return Arrays.stream(stackTrace).filter(s -> !s.getClassName().startsWith("org.testcharm.jfactory"))
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

    public ConsistencyItem<T> absoluteProperty(PropertyChain base) {
        ConsistencyItem<T> absolute = new ConsistencyItem<>(properties.stream().map(base::concat).collect(toList()), consistency, location);
        absolute.decomposer = decomposer;
        absolute.composer = composer;
        absolute.decomposerLocation = decomposerLocation;
        absolute.composerLocation = composerLocation;
        return absolute;
    }

    Resolver resolver(ObjectProducer<?> root, DefaultConsistency<T, ?>.Resolver consistency) {
        return new Resolver(root, consistency);
    }

    enum ErrorType {
        NO_ERROR, COMPOSER_ERROR, DECOMPOSER_ERROR
    }

    void dump(IndentBuffer indentBuffer, ErrorType type) {
        IndentBuffer indent = indentBuffer.append("- ").append(properties.stream().map(Objects::toString).collect(joining(", ")))
                .append(" => ").append(dumpStackTraceElement(location)).indent().newLine();
        dumpFunction(indent, "composer", composerLocation, type == ErrorType.COMPOSER_ERROR);
        dumpFunction(indent, "decomposer", decomposerLocation, type == ErrorType.DECOMPOSER_ERROR);
    }

    private void dumpFunction(IndentBuffer indent, String title, StackTraceElement stackTraceElement, boolean errorFlag) {
        if (stackTraceElement != null) {
            indent.append(title + ": ").append(dumpStackTraceElement(stackTraceElement)).newLine();
            if (errorFlag)
                indent.append(String.join("", Collections.nCopies(title.length() + 1, "^"))).newLine();
        }
    }

    class Resolver {
        private final ObjectProducer<?> root;
        private final DefaultConsistency<T, ?>.Resolver consistency;
        private Object[] cached;

        Resolver(ObjectProducer<?> root, DefaultConsistency<T, ?>.Resolver consistency) {
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
            try {
                return composer.apply(properties.stream().map(root::descendantForRead).map(Producer::getValue).toArray());
            } catch (Exception e) {
                throw new ConsistencyException(this, true, e);
            }
        }

        Object decompose(Resolver provider, int index) {
            if (cached == null) {
                cached = decomposer.apply(provider.compose());
                if (cached.length != properties.size())
                    throw new ConsistencyException(this, false,
                            String.format("Writer at %s should return an array with size %d but got an array with size %d",
                                    dumpStackTraceElement(decomposerLocation), properties.size(), cached.length));
            }
            return cached[index];
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

        DefaultConsistency<T, ?>.Resolver consistencyResolver() {
            return consistency;
        }

        String buildErrorMessageForProvider(boolean composerError, String string) {
            IndentBuffer indentBuffer = IndentBuffer.create();
            if (composerError)
                indentBuffer.append("Got an error when composing the intermediate value from the properties <");
            else
                indentBuffer.append("Got an error when decomposing the intermediate value to the properties <");

            indentBuffer.append(propertiesInfo())
                    .append(">:");
            indentBuffer.indent().newLine().append(string);
            consistency.dump(indentBuffer.newLine().newLine().append("Consistency:").indent().newLine(),
                    ConsistencyItem.this, composerError);
            return indentBuffer.toString();
        }

        String propertiesInfo() {
            return properties.stream().map(property -> root.getType().getName() + "." + property).collect(joining(", "));
        }
    }
}
