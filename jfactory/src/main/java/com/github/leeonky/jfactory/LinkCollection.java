package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

class LinkCollection {
    private final List<DefaultConsistency<?>> consistencies = new ArrayList<>();

    public void add(DefaultConsistency<?> consistency) {
        consistencies.add(consistency);
    }

    public void applyLink(Producer<?> producer) {
        LinkedList<DefaultConsistency<?>> merged = merged();
        while (!merged.isEmpty())
            popRootDependency(merged).apply(producer);
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

    private LinkedList<DefaultConsistency<?>> merged() {
        return mergeOnce(consistencies);
    }

    private LinkedList<DefaultConsistency<?>> mergeOnce(List<DefaultConsistency<?>> list) {
        LinkedList<DefaultConsistency<?>> merged = new LinkedList<>();
        for (DefaultConsistency<?> consistency : list)
            if (merged.stream().noneMatch(e -> e.merge(consistency)))
                merged.add(consistency);
        merged.forEach(DefaultConsistency::distinct);
        return merged.size() == list.size() ? merged : mergeOnce(merged);
    }

    public LinkCollection absoluteProperty(PropertyChain base) {
        LinkCollection linkCollection = new LinkCollection();
        consistencies.forEach(consistency -> linkCollection.add(consistency.absoluteProperty(base)));
        return linkCollection;
    }

    public void addAll(LinkCollection linkCollection) {
        consistencies.addAll(linkCollection.consistencies);
    }
}
