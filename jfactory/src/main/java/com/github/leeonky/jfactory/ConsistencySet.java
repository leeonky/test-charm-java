package com.github.leeonky.jfactory;

import java.util.*;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toCollection;

class ConsistencySet {
    private final List<DefaultConsistency<?>> consistencies = new ArrayList<>();

    public void add(DefaultConsistency<?> consistency) {
        consistencies.add(consistency);
    }

    public void apply(ObjectProducer<?> producer) {
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
        DefaultConsistency<?>.Resolver consistencyResolver = unResolvedConsistencies.pop();
        for (ConsistencyItem<?>.Resolver itemResolver : consistencyResolver.providers) {
            if (itemResolver.hasReadonly())
                return itemResolver;
        }
        for (ConsistencyItem<?>.Resolver itemResolver : consistencyResolver.providers) {
            if (itemResolver.hasTypeOf(UnFixedValueProducer.class))
                return itemResolver;
        }
        return consistencyResolver.providers.iterator().next();
    }

    private DefaultConsistency<?> popRootDependency(LinkedList<DefaultConsistency<?>> merged) {
        ListIterator<DefaultConsistency<?>> iterator = merged.listIterator();
        while (iterator.hasNext()) {
            DefaultConsistency<?> candidate = iterator.next();
            if (merged.stream().filter(c -> c != candidate).noneMatch(candidate::dependsOn)) {
                iterator.remove();
                return candidate;
            }
        }
        StringBuilder builder = new StringBuilder("Circular dependency detected between:");
        for (DefaultConsistency<?> defaultConsistency : merged)
            builder.append("\n").append(defaultConsistency.info());
        throw new ConflictConsistencyException(builder.toString());
    }

    private List<DefaultConsistency<?>> mergeBySameItem(List<DefaultConsistency<?>> list) {
        List<DefaultConsistency<?>> merged = new ArrayList<>();
        for (DefaultConsistency<?> consistency : list)
            if (merged.stream().noneMatch(e -> e.merge(consistency)))
                merged.add(consistency);
        return merged.size() == list.size() ? merged : mergeBySameItem(merged);
    }

    public ConsistencySet absoluteProperty(PropertyChain base) {
        ConsistencySet consistencySet = new ConsistencySet();
        consistencies.forEach(consistency -> consistencySet.add(consistency.absoluteProperty(base)));
        return consistencySet;
    }

    public void addAll(ConsistencySet consistencySet) {
        consistencies.addAll(consistencySet.consistencies);
    }
}
