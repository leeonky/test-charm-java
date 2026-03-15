package org.testcharm.pf;

import org.testcharm.dal.Accessors;

public interface Region<T extends Element<T, ?>> {
    T element();

    default <O> O perform(String expression) {
        return perform(expression, null);
    }

    default <O> O perform(String expression, Object constants) {
        return Accessors.get(expression).by(PageFlow.dal()).constants(constants).from(element());
    }

    default Elements<T> locate(String expression) {
        return locate(expression, null);
    }

    @SuppressWarnings("unchecked")
    default Elements<T> locate(String expression, Object constants) {
        Object elements = Accessors.get(expression).by(PageFlow.dal()).constants(constants).from(element());
        if (elements instanceof Elements)
            return (Elements<T>) elements;
        throw new IllegalStateException("Locate should return type Elements, but got: " + elements);
    }
}
