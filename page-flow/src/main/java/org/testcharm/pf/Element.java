package org.testcharm.pf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcharm.dal.Accessors;
import org.testcharm.dal.Evaluator;
import org.testcharm.util.BeanClass;
import org.testcharm.util.Collector;

import java.util.ArrayList;
import java.util.List;

import static org.testcharm.dal.Assertions.expect;

public interface Element<T extends Element<T, E, P>, E, P extends PageFlow> {
    Logger logger = LoggerFactory.getLogger(Element.class);

    @SuppressWarnings("unchecked")
    default T newChildren(E element) {
        return (T) BeanClass.create(getClass()).newInstance(pageFlow(), element);
    }

    List<E> findElements(By by);

    default int defaultTimeout() {
        return 8888;
    }

    int timeout();

    T patience(String time);

    @SuppressWarnings("unchecked")
    default List<By> locators() {
        return new ArrayList<By>() {{
            for (T p = (T) Element.this; p != null; p = p.parent())
                if (p.getLocator() != null)
                    add(0, p.getLocator());
        }};
    }

    String tag();

    String text();

    T click();

    T typeIn(String value);

    T clear();

    default T fillIn(Object value) {
        return clear().typeIn(String.valueOf(value));
    }

    default Collector fillIn() {
        return new ScopedJFactoryCollector(pageFlow().jFactory(), Object.class) {
            @Override
            public void onExit() {
                fillIn(build());
            }

            @Override
            public void setValue(Object value) {
                fillIn(value);
            }
        };
    }

    default boolean isInput() {
        return false;
    }

    By getLocator();

    T setLocator(By locator);

    T parent();

    T parent(T parent);

    default Object value() {
        throw new IllegalStateException("Not support operation");
    }

    byte[] screenshot();

    @SuppressWarnings("unchecked")
    default Elements<T> find(By locator) {
        return new LocatorElements<>(locator, (T) this);
    }

    default Elements<T> css(String css) {
        return find(By.css(css));
    }

    default Elements<T> caption(String text) {
        return find(By.caption(text));
    }

    default Elements<T> xpath(String xpath) {
        return find(By.xpath(xpath));
    }

    default Elements<T> placeholder(String placeholder) {
        return find(By.placeholder(placeholder));
    }

    String getDom();

    P pageFlow();

    E raw();

    boolean isEnabled();

    default <O> O perform(String expression) {
        return perform(expression, null);
    }

    default <O> O perform(String expression, Object constants) {
        return Evaluator.evaluate(expression).by(pageFlow().dal()).constants(constants).on(this);
    }

    default <O> O performAll(String expressions) {
        return performAll(expressions, null);
    }

    default <O> O performAll(String expressions, Object constants) {
        return Evaluator.evaluateAll(expressions).by(pageFlow().dal()).constants(constants).on(this);
    }

    default Elements<T> locate(String expression) {
        return locate(expression, null);
    }

    @SuppressWarnings("unchecked")
    default Elements<T> locate(String expression, Object constants) {
        Object elements = Accessors.get(expression).by(pageFlow().dal()).constants(constants).from(this);
        if (elements instanceof Elements)
            return (Elements<T>) elements;
        throw new IllegalStateException("Locate should return type Elements, but got: " + elements);
    }

    default void should(String expression) {
        should(expression, null);
    }

    default void should(String expression, Object constants) {
        expect(this).constants(constants).should(expression);
    }

    boolean isVisible();
}
