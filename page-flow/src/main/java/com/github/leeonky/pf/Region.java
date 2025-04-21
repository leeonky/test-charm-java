package com.github.leeonky.pf;

import com.github.leeonky.dal.Accessors;
import com.github.leeonky.dal.DAL;

public class Region<T extends Element<T, ?>> {
    private static final DAL dal = DAL.dal("PageFlow");
    protected final T element;

    public Region(T element) {
        this.element = element;
    }

    @Override
    public String toString() {
        return element.text();
    }

    public <O> O perform(String expression) {
        return Accessors.get(expression).by(dal).from(element);
    }
}
