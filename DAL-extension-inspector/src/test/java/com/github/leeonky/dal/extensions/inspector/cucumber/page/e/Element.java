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
        return isCssCheckbox() || super.isInput();
    }

    @Override
    public Element fillIn(Object value) {
        if (isCssCheckbox()) {
            if (!value.equals(value()))
                click();
            return this;
        }
        return super.fillIn(value);
    }

    @Override
    public Object value() {
        if (isCssCheckbox())
            return css("input").single().value();
        return super.value();
    }

    private boolean isCssCheckbox() {
        return binarySearch((String[]) attribute("class"), "switch") >= 0;
    }
}
