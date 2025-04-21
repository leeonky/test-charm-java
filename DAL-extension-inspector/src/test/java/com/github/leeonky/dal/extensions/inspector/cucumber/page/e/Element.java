package com.github.leeonky.dal.extensions.inspector.cucumber.page.e;

import com.github.leeonky.pf.PlaywrightElement;
import com.microsoft.playwright.Locator;

import static java.util.Arrays.binarySearch;

public class Element extends PlaywrightElement<Element> {
    public Element(Locator element) {
        super(element);
    }

    @Override
    public boolean isInput() {
        return isCheckedBox() || super.isInput();
    }

    @Override
    public Element typeIn(String value) {
        if (isCheckedBox()) {
            if (!value.equals(value()))
                click();
            return this;
        } else
            return super.typeIn(value);
    }

    @Override
    public Object value() {
        if (isCheckedBox())
            return css("input").single().value();
        return super.value();
    }

    private boolean isCheckedBox() {
        return binarySearch((String[]) attribute("class"), "switch") >= 0;
    }
}
