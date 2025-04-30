package com.github.leeonky.pf;

import com.github.leeonky.dal.runtime.AdaptiveList;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        if (checkAble()) {
            if (element.isSelected() != (boolean) value)
                click();
        } else if (selectAble()) {
            Select select = new Select(element);
            if (select.isMultiple())
                select.deselectAll();
            Pattern.compile("\r\n|\r|\n").splitAsStream(String.valueOf(value).trim())
                    .forEach(select::selectByVisibleText);
        } else
            super.fillIn(value);
        return (T) this;
    }

    @Override
    public Object value() {
        if (checkAble())
            return element.isSelected();
        else if (selectAble())
            return AdaptiveList.staticList(new Select(element).getAllSelectedOptions().stream()
                    .map(org.openqa.selenium.WebElement::getText).collect(Collectors.toList()));
        return WebElement.super.value();
    }
}
