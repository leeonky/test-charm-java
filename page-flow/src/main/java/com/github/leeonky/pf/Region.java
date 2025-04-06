package com.github.leeonky.pf;

public class Region<T extends Element<T, ?>> {
    protected final T element;

    public Region(T element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return element.text();
    }
}
