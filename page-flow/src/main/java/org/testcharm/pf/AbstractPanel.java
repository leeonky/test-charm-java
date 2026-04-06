package org.testcharm.pf;

//TODO need test
public class AbstractPanel<T extends Element<T, ?, ?>> implements Panel<T> {
    private final T element;

    public AbstractPanel(T element) {
        this.element = element;
    }

    @Override
    public T element() {
        return element;
    }

    @Override
    public String toString() {
        return getClass() + "\n" + element().getDom();
    }
}
