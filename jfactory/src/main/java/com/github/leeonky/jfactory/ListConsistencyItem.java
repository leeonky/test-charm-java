package com.github.leeonky.jfactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

class ListConsistencyItem<T> {
    Set<String> property = new LinkedHashSet<>();
    AbstractConsistency.Composer<T> composer;
    AbstractConsistency.Decomposer<T> decomposer;

    public ListConsistencyItem(Collection<String> property) {
        this.property = new LinkedHashSet<>(property);
    }

    public void setComposer(AbstractConsistency.Composer<T> composer) {
        this.composer = composer;
    }

    public void setDecomposer(AbstractConsistency.Decomposer<T> decomposer) {
        this.decomposer = decomposer;
    }
}