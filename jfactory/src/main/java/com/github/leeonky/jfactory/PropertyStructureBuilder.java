package com.github.leeonky.jfactory;

import com.github.leeonky.util.function.TriConsumer;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class PropertyStructureBuilder<T> {
    private final Spec<T> spec;
    private final String property;

    public PropertyStructureBuilder(Spec<T> spec, String property) {
        this.spec = spec;
        this.property = property;
    }

    public <D> DependsOn1<D> dependsOn(String property) {
        return new DependsOn1<>(property);
    }

    public <D1, D2> DependsOn2<D1, D2> dependsOn(String property1, String property2) {
        return new DependsOn2<>(property1, property2);
    }

    public DependsOn dependsOn(String... property) {
        return new DependsOn(property);
    }

    public class DependsOn {
        private final List<String> dependents;
        private Predicate<Object[]> condition = any -> true;

        public DependsOn(String[] property) {
            dependents = asList(property);
        }

        public DependsOn when(Predicate<Object[]> condition) {
            this.condition = condition;
            return this;
        }

        public Spec<T> populate(BiConsumer<PropertySpec<T>, Object[]> definition) {
            spec.appendStructureDefinition(new PropertyStructureDefinition<>(dependents, property,
                    values -> condition.test(values),
                    (ps, values) -> definition.accept(ps, values)));
            return spec;
        }
    }

    public class DependsOn1<D> {
        private final String dependent;
        private Predicate<D> condition = any -> true;

        public DependsOn1(String dependent) {
            this.dependent = dependent;
        }

        public DependsOn1<D> when(Predicate<D> condition) {
            this.condition = condition;
            return this;
        }

        public Spec<T> populate(BiConsumer<PropertySpec<T>, D> definition) {
            spec.appendStructureDefinition(new PropertyStructureDefinition<>(singletonList(dependent), property,
                    values -> condition.test((D) values[0]),
                    (ps, values) -> definition.accept(ps, (D) values[0])));
            return spec;
        }
    }

    public class DependsOn2<D1, D2> {
        private final String dependent1, dependent2;
        private BiPredicate<D1, D2> condition = (any1, any2) -> true;

        public DependsOn2(String property1, String property2) {
            this.dependent1 = property1;
            this.dependent2 = property2;
        }

        public DependsOn2<D1, D2> when(BiPredicate<D1, D2> condition) {
            this.condition = condition;
            return this;
        }

        public Spec<T> populate(TriConsumer<PropertySpec<T>, D1, D2> definition) {
            spec.appendStructureDefinition(new PropertyStructureDefinition<>(asList(dependent1, dependent2), property,
                    values -> condition.test((D1) values[0], (D2) values[1]),
                    (ps, values) -> definition.accept(ps, (D1) values[0], (D2) values[1])));
            return spec;
        }
    }
}

class PropertyStructureDefinition<T> {
    final List<PropertyChain> dependents;
    final String property;
    final BiConsumer<PropertySpec<T>, Object[]> definition;
    final Predicate<Object[]> condition;

    PropertyStructureDefinition(List<String> dependent1, String property, Predicate<Object[]> condition, BiConsumer<PropertySpec<T>, Object[]> definition) {
        this.dependents = dependent1.stream().map(PropertyChain::propertyChain).collect(toList());
        this.property = property;
        this.definition = definition;
        this.condition = condition;
    }

    void apply(Spec<T> spec, ObjectProducer<?> rootProducer) {
        Object[] dependents = this.dependents.stream().map(rootProducer::descendantForRead).map(Producer::getValue).toArray();
        if (condition.test(dependents)) {
            definition.accept(spec.property(property), dependents);
        }
    }
}