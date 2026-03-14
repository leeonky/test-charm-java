package org.testcharm.pf;

import org.testcharm.dal.extensions.basic.sync.Retryer;
import org.testcharm.dal.runtime.AdaptiveList;
import org.testcharm.dal.runtime.CollectionDALCollection;
import org.testcharm.dal.runtime.DALCollection;
import org.testcharm.dal.runtime.InvalidAdaptiveListException;
import org.testcharm.util.Sneaky;

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
            list = new Retryer(element.timeout(), 100).get(() -> {
                DALCollection<T> elements = findAll();
                if (elements.size() != 1)
                    throw unexpectedElementSize();
                return elements;
            });
        if (list.size() != 1)
            throw unexpectedElementSize();
        return list.collect();
    }

    private InvalidAdaptiveListException unexpectedElementSize() {
        return new InvalidAdaptiveListException(locateInfo("Operations can only be performed on a single located element at: ", " => " + locator));
    }

    private String locateInfo(String prefix, String action) {
        return element.locators().stream().map(By::toString).collect(Collectors.joining(" / ", prefix, action));
    }
}
