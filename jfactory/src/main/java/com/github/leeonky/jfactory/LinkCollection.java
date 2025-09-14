package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.List;

class LinkCollection {
    private final List<Consistency<?>> consistencies = new ArrayList<>();

    public void add(Consistency<?> consistency) {
        consistencies.add(consistency);
    }

    public void applyLink(Producer<?> producer) {
        for (Consistency<?> consistency : consistencies) {
            consistency.apply(producer);
        }
    }
}
