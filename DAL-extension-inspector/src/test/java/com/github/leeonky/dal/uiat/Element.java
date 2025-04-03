package com.github.leeonky.dal.uiat;

import com.github.leeonky.util.BeanClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.leeonky.util.function.Extension.not;
import static org.awaitility.Awaitility.await;

public interface Element<T extends Element<T, E>, E> {
    Logger logger = LoggerFactory.getLogger(Element.class);

    @SuppressWarnings("unchecked")
    default T newChildren(E element) {
        return (T) BeanClass.create(getClass()).newInstance(element);
    }

    List<E> findElements(By by);

    @SuppressWarnings("unchecked")
    default T findBy(By locator) {
        logger.info(locators().stream().map(By::toString).collect(Collectors.joining(" / ", "find: ", " => " + locator)));
        List<E> list = await().ignoreExceptions().until(() -> findElements(locator), not(List::isEmpty));
        if (list.size() > 1)
            throw new IllegalStateException("Found more than one elements: " + locator);
        T child = newChildren(list.get(0));
        child.parent((T) this);
        child.setLocator(locator);
        return child;
    }

    @SuppressWarnings("unchecked")
    default List<By> locators() {
        return new ArrayList<By>() {{
            for (T p = (T) Element.this; p != null; p = p.parent())
                if (p.getLocator() != null)
                    add(0, p.getLocator());
        }};
    }

    default List<T> findAllBy(By locator) {
        logger.info(locators().stream().map(By::toString).collect(Collectors.joining(" / ", "find all: ", " => " + locator)));
        List<T> elements = findElements(locator).stream().map(this::newChildren).collect(Collectors.toList());
        logger.info(String.format("Found %d elements", elements.size()));
        return elements;
    }

    String getTag();

    String text();

    T click();

    T typeIn(String value);

    T clear();

    default T fillIn(String value) {
        return clear().typeIn(value);
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
}
