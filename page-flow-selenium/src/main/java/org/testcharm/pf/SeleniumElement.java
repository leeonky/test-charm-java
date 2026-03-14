package org.testcharm.pf;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.support.ui.Select;
import org.testcharm.dal.runtime.AdaptiveList;
import org.testcharm.util.CollectionHelper;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.openqa.selenium.By.cssSelector;
import static org.testcharm.pf.By.*;

public abstract class SeleniumElement<T extends SeleniumElement<T>>
        extends AbstractElement<T, org.openqa.selenium.WebElement>
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
            CollectionHelper.asStream(value).forEach(text -> select.selectByVisibleText(String.valueOf(text)));
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

    @Override
    public String getLocation() {
        return generateFullXPath(element);
    }

    private String generateFullXPath(org.openqa.selenium.WebElement element) {
        if (element.getTagName().equals("html")) {
            return "/html[1]";
        }
        org.openqa.selenium.WebElement parent = element.findElement(org.openqa.selenium.By.xpath(".."));
        String elementTag = element.getTagName();
        int count = 0;
        int index = 1;

        for (org.openqa.selenium.WebElement sibling : parent.findElements(org.openqa.selenium.By.xpath("*"))) {
            String siblingTag = sibling.getTagName();
            if (siblingTag.equals(elementTag)) {
                if (sibling.equals(element)) {
                    index = count + 1;
                    break;
                }
                count++;
            }
        }

        return generateFullXPath(parent) + "/" + elementTag + "[" + index + "]";
    }

    @Override
    public byte[] screenshot() {
        return element.getScreenshotAs(OutputType.BYTES);
    }
}
