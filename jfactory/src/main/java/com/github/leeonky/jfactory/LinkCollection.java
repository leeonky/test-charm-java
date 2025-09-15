package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class LinkCollection {
    private final List<DefaultConsistency<?>> consistencies = new ArrayList<>();

    public void add(DefaultConsistency<?> consistency) {
        consistencies.add(consistency);
    }

    public void applyLink(Producer<?> producer) {
        for (DefaultConsistency<?> consistency : merge()) {
            consistency.apply(producer);
        }
    }

    private List<DefaultConsistency<?>> merge() {
        LinkedList<DefaultConsistency<?>> left = new LinkedList<>(consistencies);
        List<DefaultConsistency<?>> merged = new ArrayList<>();
        while (!left.isEmpty()) {
            DefaultConsistency<?> popped = left.pop();
            left.removeIf(popped::merge);
            merged.add(popped);
        }
        return merged;
    }
}
