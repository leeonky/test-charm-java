package org.testcharm.pf;

import org.testcharm.dal.Accessors;
import org.testcharm.dal.Evaluator;
import org.testcharm.dal.runtime.Data;

import java.util.function.BiFunction;

import static org.testcharm.dal.Assertions.expect;

//TODO need test
public interface Panel<E extends Element<E, ?, ?>> {
    E element();

    default <O> O perform(String expression) {
        return perform(expression, null);
    }

    default <O> O perform(String expression, Object constants) {
        return Evaluator.evaluate(expression).by(element().pageFlow().dal()).constants(constants).on(element());
    }

    default <O> O performAll(String expressions) {
        return performAll(expressions, null);
    }

    default <O> O performAll(String expressions, Object constants) {
        return Evaluator.evaluateAll(expressions).by(element().pageFlow().dal()).constants(constants).on(element());
    }

    default Elements<E> locate(String expression) {
        return locate(expression, null);
    }

    @SuppressWarnings("unchecked")
    default Elements<E> locate(String expression, Object constants) {
        Object elements = Accessors.get(expression).by(element().pageFlow().dal()).constants(constants).from(element());
        if (elements instanceof Elements)
            return (Elements<E>) elements;
        throw new IllegalStateException("Locate should return type Elements, but got: " + elements);
    }

    default void fillInBy(BiFunction<E, String, Elements<E>> by, Object data) {
        Data<?> dataWrapper = element().pageFlow().dal().getRuntimeContextBuilder().build(data).getThis();
        for (Object key : dataWrapper.fieldNames())
            by.apply(element(), (String) key).single().fillIn(dataWrapper.property(key).value());
    }

    default <O> O operate(String expression) {
        return operate(expression, null);
    }

    default <O> O operate(String expression, Object constants) {
        return Evaluator.evaluate(expression).by(element().pageFlow().dal()).constants(constants).on(this);
    }

    default <O> O operateAll(String expressions) {
        return operateAll(expressions, null);
    }

    default <O> O operateAll(String expressions, Object constants) {
        return Evaluator.evaluateAll(expressions).by(element().pageFlow().dal()).constants(constants).on(this);
    }

    default void should(String expression) {
        should(expression, null);
    }

    default void should(String expression, Object constants) {
        expect(this).constants(constants).should(expression);
    }

    default String text() {
        return element().text();
    }
}
