package com.github.leeonky.dal.extensions.inspector.cucumber.ui;

import java.util.List;
import java.util.stream.Collectors;

public abstract class SeleniumElement<T extends SeleniumElement<T, E>, E> implements WebElement<T, E> {
    protected final org.openqa.selenium.WebElement element;

    public SeleniumElement(org.openqa.selenium.WebElement element) {
        this.element = element;
    }

    @Override
    public String getText() {
        return element.getText();
    }

    @Override
    public String getTag() {
        return element.getTagName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T click() {
        element.click();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T typeIn(String value) {
        element.sendKeys(value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T clear() {
        element.clear();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> findElements(By by) {
        return element.findElements(seleniumBy(by)).stream().map(e -> newInstance((E) e)).collect(Collectors.toList());
    }

    private static org.openqa.selenium.By seleniumBy(By by) {
        switch (by.type()) {
            case "css":
                return org.openqa.selenium.By.cssSelector(by.value());
            case "text":
                return org.openqa.selenium.By.xpath(".//*[text()='" + by.value() + "']");
            case "xpath":
                return org.openqa.selenium.By.xpath(by.value());
            default:
                throw new UnsupportedOperationException("Unsupported by type: " + by.type());
        }
    }

    @Override
    public Object attribute(String name) {
        String value = element.getAttribute(name);
        return name.equals("class") ? value.split(" ") : value;
    }
}
