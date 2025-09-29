package com.github.leeonky.jfactory;

class ListConsistencyItem<T> {
    String property;
    AbstractConsistency.Composer<T> composer;
    AbstractConsistency.Decomposer<T> decomposer;

    public ListConsistencyItem(String property) {
        this.property = property;
    }

    public void setComposer(AbstractConsistency.Composer<T> composer) {
        this.composer = composer;
    }

    public void setDecomposer(AbstractConsistency.Decomposer<T> decomposer) {
        this.decomposer = decomposer;
    }
}