package com.github.leeonky.pf;

import com.github.leeonky.dal.runtime.AdaptiveList;
import com.microsoft.playwright.Locator;

import java.util.Collection;
import java.util.List;

import static com.github.leeonky.pf.By.*;
import static java.lang.String.format;

public abstract class PlaywrightElement<T extends PlaywrightElement<T>>
        extends AbstractElement<T, Locator> implements WebElement<T, Locator> {
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
            case CSS:
                return by.value();
            case XPATH:
                return "xpath=" + by.value();
            case CAPTION:
                return format("xpath=.//*[normalize-space(@value)='%s' or normalize-space(text())='%s']", by.value(), by.value());
            case PLACEHOLDER:
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
        locator.pressSequentially(value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T clear() {
        locator.clear();
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T fillIn(Object value) {
        if (checkAble()) {
            if (locator.isChecked() != (boolean) value)
                click();
        } else if (selectAble()) {
            locator.selectOption(String.valueOf(value).trim().split("\r\n|\r|\n"));
        } else
            locator.fill(String.valueOf(value));
        return (T) this;
    }

    @Override
    public Object value() {
        if (checkAble())
            return locator.isChecked();
        if (selectAble())
            return AdaptiveList.staticList((Collection<?>)
                    locator.evaluate("select => Array.from(select.selectedOptions).map(option => option.text)"));
        return locator.inputValue();
    }

    @Override
    public String getLocation() {
        return (String) locator.evaluate("element => { const getXPath = (node) => { if (node.tagName === 'HTML') return '/html[1]'; let ix=0; const siblings = node.parentNode.childNodes; for (var i=0; i<siblings.length; i++) { const sibling = siblings[i]; if (sibling === node) return getXPath(node.parentNode) + '/' + node.tagName.toLowerCase() + '[' + (ix+1) + ']'; if (sibling.nodeType === 1 && sibling.tagName === node.tagName) ix++; } }; return getXPath(element); }");
    }
}
