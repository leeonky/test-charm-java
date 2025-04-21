package com.github.leeonky.pf;

import com.github.leeonky.dal.Accessors;

public class Region<T extends Element<T, ?>> {
    protected final T element;

    public Region(T element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return element.text();
    }

    public <O> O perform(String expression) {
        return Accessors.get(expression).by(PageFlow.dal()).from(element);
    }

    public Elements<T> locate(String expression) {
        return Accessors.get(expression).by(PageFlow.dal()).from(element);
    }
}
