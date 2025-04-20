package com.github.leeonky.pf;

import com.github.leeonky.dal.extensions.basic.sync.Retryer;
import com.github.leeonky.dal.runtime.AdaptiveList;
import com.github.leeonky.dal.runtime.CollectionDALCollection;
import com.github.leeonky.dal.runtime.DALCollection;
import com.github.leeonky.util.BeanClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    default List<T> findAllBy(By locator) {
        logger.info(locators().stream().map(By::toString).collect(Collectors.joining(" / ", "Find all: ", " => " + locator)));
        List<T> elements = findElements(locator).stream().map(element -> buildChild(element, locator)).collect(Collectors.toList());
        logger.info(String.format("Found %d elements", elements.size()));
        return elements;
    }

    default T findBy(By locator) {
        logger.info(locators().stream().map(By::toString).collect(Collectors.joining(" / ", "find: ", " => " + locator)));
        List<E> list = new Retryer(defaultTimeout(), 20).get(() -> {
            List<E> elements = findElements(locator);
            if (elements.isEmpty())
                throw new IllegalStateException("No elements " + locator + " found");
            return elements;
        });
        if (list.size() > 1)
            throw new IllegalStateException("Found more than one elements: " + locator);
        return buildChild(list.get(0), locator);
    }

    @SuppressWarnings("unchecked")
    default T buildChild(E element, By locator) {
        T child = newChildren(element);
        child.parent((T) this);
        child.setLocator(locator);
        return child;
    }

    default Finder<List<T>> findAll() {
        return this::findAllBy;
    }

    interface Finder<T> {
        T by(By by);

        default T css(String css) {
            return by(By.css(css));
        }

        default T xpath(String xpath) {
            return by(By.xpath(xpath));
        }

        default T text(String text) {
            return by(By.text(text));
        }

        default T placeholder(String placeholder) {
            return by(By.placeholder(placeholder));
        }
    }

    default AdaptiveList<T> find(By locator) {
        return new AdaptiveList<T>() {

            private DALCollection<T> list;

            @Override
            public DALCollection<T> list() {
                if (list == null)
                    list = findAll();
                return list;
            }

            private CollectionDALCollection<T> findAll() {
                logger.info(locateInfo("Finding: "));
                List<E> elements = findElements(locator);
                CollectionDALCollection<T> result = new CollectionDALCollection<>(elements.stream()
                        .map(element -> buildChild(element, locator)).collect(Collectors.toList()));
                logger.info(String.format("Found %d elements", elements.size()));
                return result;
            }

            @Override
            public List<T> soloList() {
                if (list == null)
                    list = new Retryer(defaultTimeout(), 20).get(() -> {
                        DALCollection<T> elements = findAll();
                        if (elements.isEmpty())
                            throw unexpectedElementSize("no");
                        return elements;
                    });
                if (list.size() != 1)
                    throw unexpectedElementSize(list.size());
                return list.collect();
            }

            private IllegalStateException unexpectedElementSize(Object size) {
                return new IllegalStateException(String.format("%s, but %s elements were found",
                        locateInfo("Operations can only be performed on a single located element at: "), size));
            }

            private String locateInfo(String prefix) {
                return locators().stream().map(By::toString).collect(Collectors.joining(" / ", prefix, " => " + locator));
            }
        };
    }

    default AdaptiveList<T> css(String css) {
        return find(By.css(css));
    }

    default AdaptiveList<T> caption(String text) {
        return find(By.text(text));
    }
}
