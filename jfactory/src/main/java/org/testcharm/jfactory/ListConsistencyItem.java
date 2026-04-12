package org.testcharm.jfactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

class ListConsistencyItem<T> {
    private final Set<String> property;
    private DefaultConsistency.Composer<T> composer;
    private DefaultConsistency.Decomposer<T> decomposer;

    public ListConsistencyItem(Collection<String> property) {
        this.property = new LinkedHashSet<>(property);
    }

    public void setComposer(DefaultConsistency.Composer<T> composer) {
        this.composer = composer;
    }

    public void setDecomposer(DefaultConsistency.Decomposer<T> decomposer) {
        this.decomposer = decomposer;
    }

    void populateConsistency(PropertyChain elementProperty, Consistency<T, ?> consistency) {
        consistency.properties(property.stream().map(p -> elementProperty.concat(p).toString()).toArray(String[]::new))
                .read(composer).write(decomposer);
    }
}