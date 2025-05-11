package com.github.leeonky.pf;

public abstract class AbstractElement<T extends Element<T, E>, E> implements Element<T, E> {
    private By locator;
    private T parent;

    @Override
    public By getLocator() {
        return locator;
    }

    @Override
    public void setLocator(By locator) {
        this.locator = locator;
    }

    @Override
    public T parent() {
        return parent;
    }

    @Override
    public void parent(T parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return text();
    }
}
