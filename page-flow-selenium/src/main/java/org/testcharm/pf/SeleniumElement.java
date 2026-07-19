package org.testcharm.pf;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.support.ui.Select;
import org.testcharm.dal.runtime.SoloList;
import org.testcharm.io.TempDirectory;
import org.testcharm.io.VirtualFile;
import org.testcharm.util.CollectionHelper;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.openqa.selenium.By.cssSelector;
import static org.testcharm.pf.By.*;

public abstract class SeleniumElement<T extends SeleniumElement<T, P>, P extends SeleniumPageFlow>
        extends AbstractElement<T, org.openqa.selenium.WebElement, P>
        implements WebElement<T, org.openqa.selenium.WebElement, P> {

    public SeleniumElement(P pf, org.openqa.selenium.WebElement element) {
        super(pf, element);
    }

    @Override
    public String text() {
        return raw().getText();
    }

    @Override
    public String tag() {
        return raw().getTagName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T click() {
        raw().click();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T typeIn(String value) {
        raw().sendKeys(value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T clear() {
        raw().clear();
        return (T) this;
    }

    @Override
    public List<org.openqa.selenium.WebElement> findElements(By by) {
        return raw().findElements(seleniumBy(by));
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
        return raw().getAttribute(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T fillIn(Object value) {
        if (checkAble()) {
            if (raw().isSelected() != (boolean) value)
                click();
        } else if (selectAble()) {
            Select select = new Select(raw());
            if (select.isMultiple())
                select.deselectAll();
            CollectionHelper.asStream(value).forEach(text -> select.selectByVisibleText(String.valueOf(text)));
        } else if (value instanceof VirtualFile)
            super.fillIn(pageFlow().workingDir().remoteOf(TempDirectory::write, (VirtualFile) value));
        else
            super.fillIn(value);
        return (T) this;
    }

    @Override
    public Object value() {
        if (checkAble())
            return raw().isSelected();
        else if (selectAble())
            return SoloList.soloList(new Select(raw()).getAllSelectedOptions().stream()
                    .map(org.openqa.selenium.WebElement::getText).collect(Collectors.toList()));
        return WebElement.super.value();
    }

    @Override
    public byte[] screenshot() {
        return raw().getScreenshotAs(OutputType.BYTES);
    }

    @Override
    public String getDom() {
        return (String) ((JavascriptExecutor) pageFlow().webDriver()).executeScript("return arguments[0].outerHTML;", raw());
    }

    @Override
    public T download() {
        return click();
    }

    @Override
    public boolean isVisible() {
        return raw().isDisplayed();
    }

    @Override
    public boolean isEnabled() {
        return raw().isEnabled();
    }
}
