package com.github.leeonky.pf;

import java.util.List;

import static com.github.leeonky.pf.By.*;
import static java.lang.String.format;
import static org.openqa.selenium.By.cssSelector;

public abstract class SeleniumElement<T extends SeleniumElement<T>>
        extends ElementState<T, org.openqa.selenium.WebElement>
        implements WebElement<T, org.openqa.selenium.WebElement> {
    protected final org.openqa.selenium.WebElement element;

    public SeleniumElement(org.openqa.selenium.WebElement element) {
        this.element = element;
    }

    @Override
    public String text() {
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

    @Override
    public List<org.openqa.selenium.WebElement> findElements(By by) {
        return element.findElements(seleniumBy(by));
    }

    private static org.openqa.selenium.By seleniumBy(By by) {
        switch (by.type()) {
            case CSS:
                return cssSelector(by.value());
            case CAPTION:
                return org.openqa.selenium.By.xpath(format(".//*[normalize-space(@value)='%s' or normalize-space(text())='%s']", by.value(), by.value()));
            case XPATH:
                return org.openqa.selenium.By.xpath(by.value());
            case PLACEHOLDER:
                return org.openqa.selenium.By.xpath(format(".//*[@placeholder='%s']", by.value()));
            default:
                throw new UnsupportedOperationException("Unsupported find type: " + by.type());
        }
    }

    @Override
    public String attributeValue(String name) {
        return element.getAttribute(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T fillIn(Object value) {
        if (isCheckbox()) {
            if (element.isSelected() != (boolean) value)
                click();
            return (T) this;
        }
        return super.fillIn(value);
    }

    @Override
    public Object value() {
        if (isCheckbox())
            return element.isSelected();
        return WebElement.super.value();
    }
}
