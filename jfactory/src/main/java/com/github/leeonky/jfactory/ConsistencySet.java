package com.github.leeonky.jfactory;

import java.util.*;

import static com.github.leeonky.util.function.Extension.firstPresent;
import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toCollection;

class ConsistencySet {
    private final List<DefaultConsistency<?>> consistencies = new ArrayList<>();

    public void add(DefaultConsistency<?> consistency) {
        consistencies.add(consistency);
    }

    public void addAll(ConsistencySet consistencySet) {
        consistencies.addAll(consistencySet.consistencies);
    }

    public ConsistencySet absoluteProperty(PropertyChain base) {
        ConsistencySet consistencySet = new ConsistencySet();
        consistencies.forEach(consistency -> consistencySet.add(consistency.absoluteProperty(base)));
        return consistencySet;
    }

    public void resolve(ObjectProducer<?> producer) {
        LinkedList<DefaultConsistency<?>.Resolver> consistencyResolvers = mergeBySameItem(consistencies)
                .stream().map(c -> c.resolver(producer)).collect(toCollection(LinkedList::new));
        while (!consistencyResolvers.isEmpty()) {
            Set<PropertyChain> resolved = popNextRootSourceItem(consistencyResolvers).resolve();
            resolveCascaded(resolved, consistencyResolvers);
        }
    }

    private void resolveCascaded(Set<PropertyChain> resolved,
                                 LinkedList<DefaultConsistency<?>.Resolver> unResolvedConsistencies) {
        for (PropertyChain property : resolved)
            popNextRelatedItem(property, unResolvedConsistencies).ifPresent(itemResolver ->
                    resolveCascaded(itemResolver.resolve(), unResolvedConsistencies));
    }

    private Optional<ConsistencyItem<?>.Resolver> popNextRelatedItem(
            PropertyChain property, LinkedList<DefaultConsistency<?>.Resolver> unResolvedConsistencies) {
        for (DefaultConsistency<?>.Resolver consistencyResolver : unResolvedConsistencies) {
            for (ConsistencyItem<?>.Resolver provider : consistencyResolver.providers) {
                if (provider.containsProperty(property)) {
                    unResolvedConsistencies.remove(consistencyResolver);
                    return of(provider);
                }
            }
        }
        return Optional.empty();
    }

    private ConsistencyItem<?>.Resolver popNextRootSourceItem(LinkedList<DefaultConsistency<?>.Resolver> unResolvedConsistencies) {
        Optional<ConsistencyItem<?>.Resolver> firstPresent = getFirstPresent(
                () -> firstPresent(unResolvedConsistencies.stream().map(cr -> cr.searchProvider(ConsistencyItem.Resolver::hasFixed))),
                () -> firstPresent(unResolvedConsistencies.stream().map(cr -> cr.searchProvider(resolver -> resolver.hasTypeOf(ReadOnlyProducer.class)))),
                () -> firstPresent(unResolvedConsistencies.stream().map(cr -> cr.searchProvider(resolver -> resolver.hasTypeOf(UnFixedValueProducer.class)))));
        ConsistencyItem<?>.Resolver chosen = firstPresent.orElseGet(() -> unResolvedConsistencies.iterator().next().defaultProvider());
        unResolvedConsistencies.remove(chosen.consistencyResolver());
        return chosen;
    }

    private List<DefaultConsistency<?>> mergeBySameItem(List<DefaultConsistency<?>> list) {
        List<DefaultConsistency<?>> merged = new ArrayList<>();
        for (DefaultConsistency<?> consistency : list)
            if (merged.stream().noneMatch(e -> e.merge(consistency)))
                merged.add(consistency);
        return merged.size() == list.size() ? merged : mergeBySameItem(merged);
    }
}
