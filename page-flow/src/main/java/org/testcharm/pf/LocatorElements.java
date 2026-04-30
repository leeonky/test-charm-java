package org.testcharm.pf;

import org.testcharm.dal.runtime.CollectionDALCollection;
import org.testcharm.dal.runtime.DALCollection;
import org.testcharm.util.IndentBuffer;
import org.testcharm.util.Sneaky;

import java.util.List;
import java.util.stream.Collectors;

public class LocatorElements<T extends Element<T, ?, ?>> implements Elements<T> {
    private final T element;
    private final By locator;

    public LocatorElements(By locator, T element) {
        this.locator = locator;
        this.element = element;
    }

    @Override
    public DALCollection<T> list() {
        Element.logger.info("Selector: " + locateInfo(IndentBuffer.create()));
        List<?> elements = element.findElements(locator);
        Element.logger.info(String.format("Found %d elements", elements.size()));
        return new CollectionDALCollection<>(elements.stream().map(element1 -> {
            T child = element.newChildren(Sneaky.cast(element1));
            child.parent(element);
            child.setLocator(locator);
            return child;
        }).collect(Collectors.toList()));
    }

    @Override
    public int timeout() {
        return element.timeout();
    }

    @Override
    public IndentBuffer locateInfo(IndentBuffer indentBuffer) {
        return indentBuffer.appendAll(" / ", element.locators()).append(" => ").append(locator);
    }
}
