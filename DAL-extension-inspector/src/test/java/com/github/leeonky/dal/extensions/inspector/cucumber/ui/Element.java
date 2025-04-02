package com.github.leeonky.dal.extensions.inspector.cucumber.ui;

import com.github.leeonky.util.BeanClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.css;
import static com.github.leeonky.dal.extensions.inspector.cucumber.ui.By.xpath;
import static com.github.leeonky.util.function.Extension.not;
import static java.lang.String.format;
import static org.awaitility.Awaitility.await;

public interface Element<T extends Element<T, E>, E> {
    Logger logger = LoggerFactory.getLogger(Element.class);

    @SuppressWarnings("unchecked")
    default T newChildren(E element) {
        return (T) BeanClass.create(getClass()).newInstance(element);
    }

    List<E> findElements(By by);

    default T byCss(String css) {
        return by(css(css));
    }

    default T byXpath(String xpath) {
        return by(xpath(xpath));
    }

    default T byText(String text) {
        return by(By.text(text));
    }

    default T byPlaceholder(String placeholder) {
        return byXpath(format(".//*[@placeholder='%s']", placeholder));
    }

    @SuppressWarnings("unchecked")
    default T by(By locator) {
        List<By> locators = new ArrayList<>();
        for (T p = (T) this; p != null; p = p.parent())
            if (p.getLocator() != null)
                locators.add(0, p.getLocator());
        locators.add(locator);
        StringBuilder message = new StringBuilder("locate: ");
        message.append(locators.stream().map(By::toString).collect(Collectors.joining(" / ")));
        logger.info(message.toString());

        List<E> list = await().ignoreExceptions().until(() -> findElements(locator), not(List::isEmpty));
        if (list.size() > 1)
            throw new IllegalStateException("Found more than one elements: " + locator);
        T child = newChildren(list.get(0));
        child.parent((T) this);
        child.setLocator(locator);
        return child;
    }

    default List<T> allBy(By locator) {
        List<By> locators = new ArrayList<>();
        for (T p = (T) this; p != null; p = p.parent())
            if (p.getLocator() != null)
                locators.add(0, p.getLocator());
        locators.add(locator);
        StringBuilder message = new StringBuilder("locate all: ");
        message.append(locators.stream().map(By::toString).collect(Collectors.joining(" / ")));
        logger.info(message.toString());
        List<T> elements = findElements(locator).stream().map(this::newChildren).collect(Collectors.toList());
        logger.info(String.format("located %d elements", elements.size()));
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
