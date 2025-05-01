package com.github.leeonky.pf;

import com.github.leeonky.dal.extensions.basic.sync.Retryer;
import com.github.leeonky.dal.runtime.AdaptiveList;
import com.github.leeonky.dal.runtime.CollectionDALCollection;
import com.github.leeonky.dal.runtime.DALCollection;
import com.github.leeonky.dal.runtime.InvalidAdaptiveListException;
import com.github.leeonky.util.Sneaky;

import java.util.List;
import java.util.stream.Collectors;

public class Elements<T extends Element<T, ?>> implements AdaptiveList<T> {
    private final T element;
    private final By locator;
    private DALCollection<T> list;

    public Elements(By locator, T element) {
        this.locator = locator;
        this.element = element;
    }

    @Override
    public DALCollection<T> list() {
        if (list == null)
            list = findAll();
        return list;
    }

    private CollectionDALCollection<T> findAll() {
        Element.logger.info(locateInfo("Locating: ", " => " + locator));
        List<?> elements = element.findElements(locator);
        CollectionDALCollection<T> result = new CollectionDALCollection<>(elements.stream()
                .map(element -> {
                    T child = this.element.newChildren(Sneaky.cast(element));
                    child.parent(this.element);
                    child.setLocator(locator);
                    return child;
                }).collect(Collectors.toList()));
        Element.logger.info(String.format("Found %d elements", elements.size()));
        return result;
    }

    @Override
    public List<T> soloList() {
        if (list == null)
            list = new Retryer(element.defaultTimeout(), 100).get(() -> {
                DALCollection<T> elements = findAll();
                if (elements.isEmpty())
                    throw unexpectedElementSize("no");
                return elements;
            });
        if (list.size() != 1)
            throw unexpectedElementSize(list.size());
        return list.collect();
    }

    private InvalidAdaptiveListException unexpectedElementSize(Object size) {
        return new InvalidAdaptiveListException(String.format("%s, but %s elements were found",
                locateInfo("Operations can only be performed on a single located element at: ", " => " + locator), size));
    }

    private String locateInfo(String prefix, String action) {
        return element.locators().stream().map(By::toString).collect(Collectors.joining(" / ", prefix, action));
    }
}
