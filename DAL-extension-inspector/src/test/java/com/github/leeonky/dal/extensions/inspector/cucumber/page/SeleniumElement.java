package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class SeleniumElement<D extends SeleniumElement<D, E>, E> implements Element<D, E> {
    protected final WebElement element;

    public SeleniumElement(WebElement element) {
        this.element = element;
    }

    @Override
    public String getText() {
        return element.getText();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<D> findElements(By by) {
        return element.findElements(by).stream().map(e -> newInstance((E) e)).collect(Collectors.toList());
    }
}
