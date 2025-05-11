package com.github.leeonky.pf;

public class AbstractRegion<T extends Element<T, ?>> implements Region<T> {
    private final T element;

    public AbstractRegion(T element) {
        this.element = element;
    }

    @Override
    public T element() {
        return element;
    }

    @Override
    public String toString() {
        return element().text();
    }
}
