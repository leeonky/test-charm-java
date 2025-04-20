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
        return find(locator).list().collect();
    }

    default T findBy(By locator) {
        return find(locator).only();
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

            @SuppressWarnings("unchecked")
            private CollectionDALCollection<T> findAll() {
                logger.info(locateInfo("Finding: "));
                List<E> elements = findElements(locator);
                CollectionDALCollection<T> result = new CollectionDALCollection<>(elements.stream()
                        .map(element -> {
                            T child = newChildren(element);
                            child.parent((T) Element.this);
                            child.setLocator(locator);
                            return child;
                        }).collect(Collectors.toList()));
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
        return find(By.caption(text));
    }

    default AdaptiveList<T> xpath(String xpath) {
        return find(By.xpath(xpath));
    }

    default AdaptiveList<T> placeholder(String placeholder) {
        return find(By.placeholder(placeholder));
    }
}
