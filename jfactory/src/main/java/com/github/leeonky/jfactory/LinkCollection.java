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
        LinkedList<DefaultConsistency<?>> left = new LinkedList<>(consistencies);
        LinkedList<DefaultConsistency<?>> merged = new LinkedList<>();
        while (!left.isEmpty()) {
            DefaultConsistency<?> popped = left.pop();
            left.removeIf(popped::merge);
            merged.add(popped);
        }
        return merged;
    }
}
