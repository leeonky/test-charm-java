package org.testcharm.pf;

import org.testcharm.dal.extensions.basic.sync.Retryer;
import org.testcharm.dal.runtime.AdaptiveList;
import org.testcharm.dal.runtime.DALCollection;
import org.testcharm.dal.runtime.InvalidAdaptiveListException;
import org.testcharm.dal.runtime.IterableDALCollection;
import org.testcharm.util.Sneaky;

import java.util.List;
import java.util.stream.Collectors;

public class Elements<T extends Element<T, ?, ?>> implements AdaptiveList<T> {
    private final T element;
    private final By locator;

    public Elements(By locator, T element) {
        this.locator = locator;
        this.element = element;
    }

    @Override
    public DALCollection<T> list() {
        return new IterableDALCollection<>(() -> {
            Element.logger.info(locateInfo("Locating: ", " => " + locator));
            List<?> elements = element.findElements(locator);
            Element.logger.info(String.format("Found %d elements", elements.size()));
            return elements.stream().map(element1 -> {
                T child = element.newChildren(Sneaky.cast(element1));
                child.parent(element);
                child.setLocator(locator);
                return child;
            }).iterator();
        });
    }

    private String locateInfo(String prefix, String action) {
        return element.locators().stream().map(By::toString).collect(Collectors.joining(" / ", prefix, action));
    }

    @Override
    public T single() {
        try {
            return new Retryer(element.timeout(), 100).get(AdaptiveList.super::single);
        } catch (InvalidAdaptiveListException ig) {
            throw new InvalidAdaptiveListException(locateInfo("Operations can only be performed on a single located element at: ", " => " + locator), ig.list());
        }
    }
}
