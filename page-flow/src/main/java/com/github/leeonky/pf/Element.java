package com.github.leeonky.pf;

import com.github.leeonky.util.BeanClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public interface Element<T extends Element<T, E>, E> {
    Logger logger = LoggerFactory.getLogger(Element.class);

    @SuppressWarnings("unchecked")
    default T newChildren(E element) {
        return (T) BeanClass.create(getClass()).newInstance(element);
    }

    List<E> findElements(By by);

    default int defaultTimeout() {
        return 2000;
    }

    @SuppressWarnings("unchecked")
    default List<By> locators() {
        return new ArrayList<By>() {{
            for (T p = (T) Element.this; p != null; p = p.parent())
                if (p.getLocator() != null)
                    add(0, p.getLocator());
        }};
    }

    String getTag();

    String text();

    T click();

    T typeIn(String value);

    T clear();

    default T fillIn(Object value) {
        return clear().typeIn(String.valueOf(value));
    }

    default boolean isInput() {
        return false;
    }

    By getLocator();

    void setLocator(By locator);

    T parent();

    void parent(T parent);

    default Object value() {
        throw new IllegalStateException("Not support operation");
    }

    @SuppressWarnings("unchecked")
    default Elements<T> find(By locator) {
        return new Elements<>(locator, (T) this);
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
}
