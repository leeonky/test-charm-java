package com.github.leeonky.pf;

import com.microsoft.playwright.Locator;

import java.util.List;

import static java.lang.String.format;

public abstract class PlaywrightElement<T extends PlaywrightElement<T>>
        extends ElementState<T, Locator> implements WebElement<T, Locator> {
    protected final Locator locator;

    protected PlaywrightElement(Locator locator) {
        this.locator = locator;
    }

    @Override
    public String attributeValue(String name) {
        return locator.getAttribute(name);
    }

    @Override
    public List<Locator> findElements(By by) {
        return locator.locator(locateInfo(by)).all();
    }

    private String locateInfo(By by) {
        switch (by.type()) {
            case "css":
                return by.value();
            case "xpath":
                return "xpath=" + by.value();
            case "text":
                return format("xpath=.//*[normalize-space(@value)='%s' or normalize-space(text())='%s']", by.value(), by.value());
            case "placeholder":
                return format("xpath=.//*[@placeholder='%s']", by.value());
            default:
                throw new UnsupportedOperationException("Unsupported find type: " + by.type());
        }
    }

    @Override
    public String getTag() {
        return locator.evaluate("el => el.tagName").toString().toLowerCase();
    }

    @Override
    public String text() {
        return locator.evaluate("el => el.innerText").toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T click() {
        locator.click();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T typeIn(String value) {
        locator.type(value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T fillIn(String value) {
        locator.fill(value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T clear() {
        locator.fill("");
        return (T) this;
    }

    @Override
    public Object value() {
        return locator.inputValue();
    }
}
