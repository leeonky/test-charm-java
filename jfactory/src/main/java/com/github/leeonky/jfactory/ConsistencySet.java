package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.github.leeonky.util.Sneaky.cast;
import static com.github.leeonky.util.function.Extension.*;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

class ConsistencySet {
    private final List<DefaultConsistency<?>> consistencies = new ArrayList<>();

    void add(DefaultConsistency<?> consistency) {
        consistencies.add(consistency);
    }

    void addAll(ConsistencySet consistencySet) {
        consistencies.addAll(consistencySet.consistencies);
    }

    ConsistencySet absoluteProperty(PropertyChain base) {
        ConsistencySet consistencySet = new ConsistencySet();
        consistencies.forEach(consistency -> consistencySet.add(consistency.absoluteProperty(base)));
        return consistencySet;
    }

    void resolve(ObjectProducer<?> producer) {
        new Resolver(producer).resolve();
    }

    class Resolver {
        private final LinkedList<DefaultConsistency<?>.Resolver> unResolved;

        Resolver(ObjectProducer<?> producer) {
            unResolved = mergeBySameItem(processListConsistency(producer)).stream().map(c -> c.resolver(producer))
                    .collect(toCollection(LinkedList::new));
        }

        private List<DefaultConsistency<?>> processListConsistency(ObjectProducer<?> producer) {
            return consistencies.stream().map(dc -> dc.processListConsistency(producer)).collect(toList());
        }

        private List<DefaultConsistency<?>> mergeBySameItem(List<DefaultConsistency<?>> list) {
            List<DefaultConsistency<?>> merged = new ArrayList<>();
            for (DefaultConsistency<?> consistency : list)
                if (merged.stream().noneMatch(e -> e.merge(consistency)))
                    merged.add(consistency);
            return merged.size() == list.size() ? merged : mergeBySameItem(merged);
        }

        @SafeVarargs
        private final ConsistencyItem<?>.Resolver searchNextRootSourceProvider(Predicate<ConsistencyItem<?>.Resolver>... conditions) {
            return mapFirst(conditions, c -> firstPresent(unResolved.stream().map(cr -> cr.searchProvider(c))))
                    .orElseGet(() -> cast(unResolved.iterator().next().defaultProvider()));
        }

        private void resolveCascaded(Set<PropertyChain> resolved) {
            for (PropertyChain property : resolved)
                mapPresent(unResolved, consistencyResolver -> consistencyResolver.propertyRelated(property))
                        .collect(toList()).forEach(itemResolver -> resolveCascaded(pop(itemResolver).resolveAsProvider()));
        }

        private ConsistencyItem<?>.Resolver pop(ConsistencyItem<?>.Resolver itemResolver) {
            unResolved.remove(itemResolver.consistencyResolver());
            return itemResolver;
        }

        void resolve() {
            while (!unResolved.isEmpty()) {
                ConsistencyItem<?>.Resolver nextRootProvider = searchNextRootSourceProvider(ConsistencyItem.Resolver::hasFixed,
                        resolver -> resolver.hasTypeOf(ReadOnlyProducer.class),
                        resolver -> resolver.hasTypeOf(UnFixedValueProducer.class),
                        resolver -> resolver.hasTypeOf(ObjectProducer.class),
                        resolver -> resolver.hasTypeOf(DefaultValueProducer.class));
                resolveCascaded(pop(nextRootProvider).resolveAsProvider());
            }
        }
    }
}
