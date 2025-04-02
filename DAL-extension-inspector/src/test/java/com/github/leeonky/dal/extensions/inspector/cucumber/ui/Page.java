package com.github.leeonky.dal.extensions.inspector.cucumber.ui;

public class Page<T extends Element<T, ?>> {
    protected final T region;

    public Page(T region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return region.text();
    }
}
