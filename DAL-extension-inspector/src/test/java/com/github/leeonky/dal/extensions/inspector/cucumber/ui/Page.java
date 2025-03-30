package com.github.leeonky.dal.extensions.inspector.cucumber.ui;

public class Page<T extends Element<T, ?>> {
    protected final T element;

    public Page(T element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return element.getText();
    }
}
